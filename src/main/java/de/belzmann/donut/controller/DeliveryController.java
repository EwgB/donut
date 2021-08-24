package de.belzmann.donut.controller;

import de.belzmann.donut.model.Order;
import de.belzmann.donut.service.OrderService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeliveryController {

    private final OrderService service;

    DeliveryController(OrderService service) {
        this.service = service;
    }

    /**
     * Returns a list of orders for the next delivery. Orders are returned
     * in their priority queue order (ordered by premium customers and order
     * timestamp). Each delivery can at most contain 50 donuts, and orders
     * can neither be split nor changed.
     */
    @GetMapping("/nextDelivery")
    List<Order> getNextDelivery() {
        return service.getNextDelivery();
    }

    /**
     * Finishes a delivery by deleting the orders from the database.
     * Its necessary to call this after finishing a delivery because otherwise
     * subsequent calls to {@see #getNextDelivery} will return the same list
     * of orders.
     */
    @DeleteMapping("/nextDelivery")
    void finishDelivery() {
        service.finishDelivery();
    }
}
