package org.example;


// 8
public class NumberExceedsLimitException extends InvalidCommandException {
    public NumberExceedsLimitException(final String message) {
        super(message);
    }
}
