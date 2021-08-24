package de.belzmann.donut.controller;

import de.belzmann.donut.model.OrderQueueEntry;
import de.belzmann.donut.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
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

    // Aggregate root
    // tag::get-aggregate-root[]
    /**
     * Returns all order in their priority, together with their position in the queue and estimated wait time.
     * @param maxCount The maximal amount of orders to be returned. If not present, all orders are returned.
     */
    @GetMapping("/orders")
    List<OrderQueueEntry> all(@RequestParam Optional<Integer> maxCount) {
        return service.getAllOrderQueueEntries(maxCount);
    }
    // end::get-aggregate-root[]

}