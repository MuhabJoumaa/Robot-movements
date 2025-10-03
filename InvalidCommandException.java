package org.example;

public class InvalidCommandException extends Exception {
    public InvalidCommandException(final String message) {
        super(message);
    }
}
