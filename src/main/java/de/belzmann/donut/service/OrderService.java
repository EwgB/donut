package de.belzmann.donut.service;

import de.belzmann.donut.model.Order;
import de.belzmann.donut.model.OrderDto;
import de.belzmann.donut.model.OrderRepository;
import de.belzmann.donut.model.exceptions.MultipleOrdersException;
import de.belzmann.donut.model.exceptions.OrderNotFoundException;
import de.belzmann.donut.model.exceptions.OrderTooLargeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for managing orders. Is used by the {@link de.belzmann.donut.controller.OrderController}
 * for adding, deleting and reading orders, and by {@link de.belzmann.donut.controller.DeliveryController}
 * for gathering the next delivery.
 */
@Service
public class OrderService {

    /**
     * The (assumed) interval between deliveries.
     * Used to calculate the approximate wait time for orders.
     */
    private static final Duration DELIVERY_INTERVAL = Duration.of(5, ChronoUnit.MINUTES);

    /**
     * The maximal size of donuts in one delivery. Since no delivery can contain more than 50 donuts,
     * and orders can't be split for a delivery, an order can conversely also have no more than 50 donuts.
     */
    public static final int MAX_DELIVERY_SIZE = 50;

    private final OrderRepository repository;

    @PersistenceContext
    private EntityManager em;

    /**
     * Stores the time of the last delivery.
     * Used to calculate the approximate wait time for orders.
     * TODO: This should be stored in the database so that the service remains stateless.
     */
    private Instant lastDeliveryTime;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
        this.lastDeliveryTime = Instant.now();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrderQueueEntries() {
        try (Stream<OrderDto> orders = getAllOrderQueueEntriesInternal()) {
            return orders.collect(Collectors.toList());
        }
    }

    @Transactional
    public OrderDto addNewOrder(int clientId, int donutQuantity)
            throws MultipleOrdersException, OrderNotFoundException, OrderTooLargeException {
        // Check if the order is too big. Since the cart can only hold MAX_DELIVERY_SIZE
        // and orders can't be split for delivery, an order can't be larger than
        // MAX_DELIVERY_SIZE, too.
        if (donutQuantity > MAX_DELIVERY_SIZE) {
            throw new OrderTooLargeException();
        }

        // Check whether there is already an order for the client. Only one order per client is permitted.
        if (repository.existsByClientId(clientId)) {
            throw new MultipleOrdersException();
        }

        // Create and save the order. Refresh the entity so that the state of the derived priority column is correct.
        final Order newOrder = repository.save(new Order(clientId, donutQuantity, Timestamp.from(Instant.now())));
        em.refresh(newOrder);

        return getOrderById(newOrder.getOrderId());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(int id) throws OrderNotFoundException {
        return findOrderWithPredicate(orderDto -> id == orderDto.orderId);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByCustomerId(int id) throws OrderNotFoundException {
        return findOrderWithPredicate(orderDto -> id == orderDto.clientId);
    }

    /**
     * Searches for a specific order with the specified predicate.
     *
     * @param predicate A function that is used to evaluate whether a given order matches the desired one.
     * @return The found order
     */
    private OrderDto findOrderWithPredicate(Predicate<? super OrderDto> predicate)
            throws OrderNotFoundException {
        /* Because we need to determine the position of the order in the queue
         * and the estimated wait time, we can't just fetch the requested order
         * from the database. We actually have to get all previous orders, too,
         * since this data is not stored in the database.
         * This might be a costly operation depending on the application, but is
         * acceptable in this case, considering the queue shouldn't get too long.
         */
        try (Stream<OrderDto> orders = getAllOrderQueueEntriesInternal()) {
            return orders
                    .filter(predicate)
                    .findAny()
                    .orElseThrow(OrderNotFoundException::new);
        }
    }

    /**
     * Reads a stream of all orders in the database and converts them into OrderDto objects.
     * The orders are returned from the database already sorted in the correct queue order.
     * The conversion into DTOs determines the queue position (which is obviously not stored
     * in the database explicitly, since it would change on every insert or delete) and the
     * approximate wait time (this is also not in the database).
     */
    private Stream<OrderDto> getAllOrderQueueEntriesInternal() {
        // Counts the position of the order in the queue
        final AtomicInteger queuePosition = new AtomicInteger(1);

        final Instant nextDelivery = lastDeliveryTime.plus(DELIVERY_INTERVAL);

        // Tracks the approximate wait time for orders. It is initialized with the difference between now and
        // the next presumed delivery time (based on the last delivery), and incremented by DELIVERY_INTERVAL
        // with every order that wouldn't fit into the delivery.
        final AtomicLong approxWaitTime = new AtomicLong(Duration.between(Instant.now(), nextDelivery).toSeconds());

        // Tracks the size of a delivery. Every time an order would go over the maximum delivery size, this gets
        // set back to the size of the current order and the wait time is incremented by DELIVERY_INTERVAL.
        final AtomicInteger deliverySize = new AtomicInteger(0);

        return repository.findAllOrdersByPriority()
                .map(order -> {
                    // Check if this order would fit into the current delivery and compute the approximate wait time
                    // accordingly (under the assumption that the delivery is every DELIVERY_INTERVAL).
                    int newDeliverySize = deliverySize.get() + order.getDonutQuantity();
                    long currentWaitTime;
                    if (newDeliverySize <= MAX_DELIVERY_SIZE) {
                        // Order would fit, increment size
                        deliverySize.set(newDeliverySize);
                        currentWaitTime = approxWaitTime.get();
                    } else {
                        // Order wouldn't fit, reset size and increment wait time
                        deliverySize.set(order.getDonutQuantity());
                        currentWaitTime = approxWaitTime.addAndGet(DELIVERY_INTERVAL.toSeconds());
                    }
                    Duration waitDuration = Duration.of(currentWaitTime, ChronoUnit.SECONDS);
                    String waitDurationString = String.format("%d:%02d", waitDuration.toMinutesPart(), waitDuration.toSecondsPart());
                    return new OrderDto(order, queuePosition.getAndIncrement(), waitDurationString);
                });
    }

    /**
     * Deletes an order for a particular client
     *
     * @param clientId The id of the client for whom the order is supposed to be deleted.
     * @throws OrderNotFoundException thrown when there are no orders for the specified customer
     */
    @Transactional
    public void deleteOrderByCustomerId(int clientId) throws OrderNotFoundException {
        if (repository.existsByClientId(clientId)) {
            repository.deleteByClientId(clientId);
        } else {
            throw new OrderNotFoundException();
        }
    }
}
