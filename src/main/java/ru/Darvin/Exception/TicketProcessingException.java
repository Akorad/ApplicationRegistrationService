package ru.Darvin.Exception;

public class TicketProcessingException extends RuntimeException {
    public TicketProcessingException(String message) {
        super(message);
    }
}
