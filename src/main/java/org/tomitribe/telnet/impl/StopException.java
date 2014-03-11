package org.tomitribe.telnet.impl;

public class StopException extends RuntimeException {
    public StopException() {
    }

    public StopException(Throwable cause) {
        super(cause);
    }
}