package de.belzmann.donut.model;

public class Order {
    private final int clientId;
    private final int donutQuantity;

    public Order(int clientId, int donutQuantity) {
        this.clientId = clientId;
        this.donutQuantity = donutQuantity;
    }

    public int getClientId() {
        return clientId;
    }

    public int getDonutQuantity() {
        return donutQuantity;
    }
}
