package de.belzmann.donut.model;

import javax.persistence.*;

@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;

    @Column(nullable = false)
    private int clientId;

    @Column(nullable = false)
    private int donutQuantity;

    public Order() {
    }

    public Order(int clientId, int donutQuantity) {
        this.clientId = clientId;
        this.donutQuantity = donutQuantity;
    }

    public int getOrderId() {
        return orderId;
    }

    public Order setOrderId(int orderId) {
        this.orderId = orderId;
        return this;
    }

    public int getClientId() {
        return clientId;
    }

    public Order setClientId(int clientId) {
        this.clientId = clientId;
        return this;
    }

    public int getDonutQuantity() {
        return donutQuantity;
    }

    public Order setDonutQuantity(int donutQuantity) {
        this.donutQuantity = donutQuantity;
        return this;
    }
}
