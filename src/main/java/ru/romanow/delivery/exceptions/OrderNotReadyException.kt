package ru.romanow.delivery.exceptions;

public class OrderNotReadyException
        extends RuntimeException {
    public OrderNotReadyException(String message) {
        super(message);
    }
}
