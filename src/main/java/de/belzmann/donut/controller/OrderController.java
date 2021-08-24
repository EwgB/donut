package de.belzmann.donut.controller;

import de.belzmann.donut.model.OrderQueueEntry;
import de.belzmann.donut.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class OrderController {

    OrderService service;

    OrderController(OrderService service) {
        this.service = service;
    }

    /**
     * Returns all order in their priority, together with their position in the queue and estimated wait time.
     *
     * @param maxCount The maximal amount of orders to be returned. If not present, all orders are returned.
     */
    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/orders")
    List<OrderQueueEntry> all(@RequestParam Optional<Integer> maxCount) {
        return service.getAllOrderQueueEntries(maxCount);
    }
    // end::get-aggregate-root[]

    /**
     * Adds a new order to the queue. The priority is determined by the client id.
     *
     * @param clientId The id of the client.
     * @param quantity The quantity of the donuts that the client ordered.
     * @return the added order with the queue position and the approximate wait time
     */
    @PostMapping("/orders")
    OrderQueueEntry newOrder(@RequestParam int clientId, @RequestParam int quantity) {
        return service.addNewOrder(clientId, quantity);
    }
}