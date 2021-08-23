package de.belzmann.donut.controller;

import de.belzmann.donut.model.OrderQueueEntry;
import de.belzmann.donut.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    OrderService service;

    OrderController(OrderService service) {
        this.service = service;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/orders")
    List<OrderQueueEntry> all() {
        return service.getAllOrderQueueEntries();
    }
    // end::get-aggregate-root[]

}