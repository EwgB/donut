package de.belzmann.donut.model;

import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {

    /**
     * The cutoff for premium customers. Customers with id
     * lower than the cutoff are considered premium and their orders
     * are processed before the regular customers.
     */
    public static final int PREMIUM_CLIENT_CUTOFF = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "order_id")
    private Integer orderId;

    @Column(nullable = false, name = "client_id")
    private int clientId;

    @Column(nullable = false, name = "donut_quantity")
    private int donutQuantity;

    @Column(nullable = false, name = "order_time")
    private Timestamp orderTime;

    /**
     * Determines whether the order is for a premium customer.
     * This is info is derived from the customer number and
     * is not stored in the database.
     */
    @Formula("client_id < " + PREMIUM_CLIENT_CUTOFF)
    private Boolean isPriority;

    public Order() {
    }

    public Order(int clientId, int donutQuantity, Timestamp orderTime) {
        this.clientId = clientId;
        this.donutQuantity = donutQuantity;
        this.orderTime = orderTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return clientId == order.clientId &&
                donutQuantity == order.donutQuantity &&
                Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, clientId, donutQuantity);
    }

    public Integer getOrderId() {
        return orderId;
    }

    public Order setOrderId(Integer orderId) {
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

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public Order setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
        return this;
    }

    public Boolean isPriority() {
        return isPriority;
    }
}
