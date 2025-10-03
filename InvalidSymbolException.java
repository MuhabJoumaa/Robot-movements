package org.example;

// 1
public class InvalidSymbolException extends InvalidCommandException {
    public InvalidSymbolException(final String message) {
        super(message);
    }
}
