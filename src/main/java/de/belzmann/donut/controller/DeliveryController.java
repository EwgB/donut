package de.belzmann.donut.controller;

import de.belzmann.donut.model.OrderRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeliveryController {

    private final OrderRepository repository;

    DeliveryController(OrderRepository repository) {
        this.repository = repository;
    }

}
