package de.belzmann.donut.model;

/**
 * Contains an order and additional information that pertains
 * to the orders place in the queue (e.g. queue position and approximate
 * wait time) and should not be stored with the order itself.
 */
public class OrderDto {
    public final int orderId;
    public final int clientId;
    public final int donutQuantity;
    public final boolean isPriority;
    public final int queuePosition;
    public final String approximateWaitTime;

    public OrderDto(Order order, int queuePosition, String approximateWaitTime) {
        this.orderId = order.getOrderId();
        this.clientId = order.getClientId();
        this.donutQuantity = order.getDonutQuantity();
        this.isPriority = order.isPriority();
        this.queuePosition = queuePosition;
        this.approximateWaitTime = approximateWaitTime;
    }
}
