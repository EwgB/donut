package de.belzmann.donut.controller;

import de.belzmann.donut.model.OrderRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderRepository repository;

    OrderController(OrderRepository repository) {
        this.repository = repository;
    }

}
