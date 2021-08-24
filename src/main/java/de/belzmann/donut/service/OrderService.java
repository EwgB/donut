package de.belzmann.donut.service;

import de.belzmann.donut.model.Order;
import de.belzmann.donut.model.OrderDto;
import de.belzmann.donut.model.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
    private static final int MAX_DELIVERY_SIZE = 50;

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
    public List<OrderDto> getAllOrderQueueEntries(Optional<Integer> maxCount) {
        try (Stream<OrderDto> orders = getAllOrderQueueEntriesInternal()) {
            return maxCount
                    .map(orders::limit)
                    .orElse(orders)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public OrderDto addNewOrder(int clientId, int donutQuantity) {
        // Check whether there is already an order for the client. Only one order per client is permitted.
        if (repository.existsByClientId(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only one order per client is permitted.");
        }

        // Create and save the order. Refresh the entity so that the state of the derived column is correct.
        final Order newOrder = repository.save(new Order(clientId, donutQuantity, Timestamp.from(Instant.now())));
        em.refresh(newOrder);

        // TODO: Determine correct queue position and wait time
        return new OrderDto(newOrder, 1, "test");
    }

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
}
