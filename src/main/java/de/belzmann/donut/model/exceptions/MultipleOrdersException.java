package de.belzmann.donut.model.exceptions;

/**
 * Exception representing that an order was tried to be added
 * for a client that already has an order in the queue.
 */
public class MultipleOrdersException extends Exception {
}
