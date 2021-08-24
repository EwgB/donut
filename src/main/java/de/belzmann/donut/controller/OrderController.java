package de.belzmann.donut.controller;

import de.belzmann.donut.model.OrderDto;
import de.belzmann.donut.model.exceptions.MultipleOrdersException;
import de.belzmann.donut.model.exceptions.OrderNotFoundException;
import de.belzmann.donut.model.exceptions.OrderTooLargeException;
import de.belzmann.donut.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class OrderController {

    OrderService service;

    OrderController(OrderService service) {
        this.service = service;
    }

    /**
     * Returns all order in their priority, together with their position in the queue and estimated wait time.
     */
    @GetMapping("/orders")
    List<OrderDto> getAllOrders() {
        return service.getAllOrderQueueEntries();
    }

    /**
     * Adds a new order to the queue. The priority is determined by the client id.
     *
     * @param clientId The id of the client.
     * @param quantity The quantity of the donuts that the client ordered.
     * @return the added order with the queue position and the approximate wait time
     */
    @PostMapping("/orders")
    OrderDto newOrder(@RequestParam int clientId, @RequestParam int quantity) {
        try {
            return service.addNewOrder(clientId, quantity);
        } catch (OrderTooLargeException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    String.format("The order is too large, orders can't exceed %d donuts.", OrderService.MAX_DELIVERY_SIZE));
        } catch (MultipleOrdersException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only one order per client is permitted.");
        } catch (OrderNotFoundException e) {
            // This shouldn't happen. We just added the order, and then can't find it?
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Something went wrong adding the order.");
        }
    }

    /**
     * Returns a single order by its ID, or 404 if not found
     *
     * @param id The order id
     * @return the order
     */
    @GetMapping("/orders/{id}")
    OrderDto getOrderById(@PathVariable int id) {
        try {
            return service.getOrderById(id);
        } catch (OrderNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Order with ID %d not found.", id));

        }
    }

    /**
     * Returns a single order by the client ID, or 404 if not found
     *
     * @param clientId The client id
     * @return the order
     */
    @GetMapping(value = "/orders", params = "clientId")
    OrderDto getOrderByCustomerId(@RequestParam int clientId) {
        try {
            return service.getOrderByCustomerId(clientId);
        } catch (OrderNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("No order for customer with ID %d not found.", clientId));
        }
    }

    /**
     * Deletes an order for a particular client. Returns a 404 error when no order for customer is found.
     *
     * @param clientId The id of the client for whom the order is supposed to be deleted.
     */
    @DeleteMapping("/orders")
    void deleteOrderByCustomerId(@RequestParam int clientId) {
        try {
            service.deleteOrderByCustomerId(clientId);
        } catch (OrderNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("No order for customer with ID %d not found.", clientId));
        }
    }
}