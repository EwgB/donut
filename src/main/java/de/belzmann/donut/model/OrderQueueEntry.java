package de.belzmann.donut.model;

/**
 * Contains an order and additional information that pertains
 * to the orders place in the queue (e.g. queue position and approximate
 * wait time) and should not be stored with the order itself.
 */
public class OrderQueueEntry {
    private final Order order;
    private final int queuePosition;
    private final String approximateWaitTime;

    public OrderQueueEntry(Order order, int queuePosition, String approximateWaitTime) {
        this.order = order;
        this.queuePosition = queuePosition;
        this.approximateWaitTime = approximateWaitTime;
    }

    public Order getOrder() {
        return order;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public String getApproximateWaitTime() {
        return approximateWaitTime;
    }
}
