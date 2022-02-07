package com.beyt.generator.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by tdilber at 6/25/2020
 */
@Slf4j
public class NoAnnotationException extends RuntimeException {
    public NoAnnotationException() {
        super();
    }

    public NoAnnotationException(String message) {
        super(message);
    }

    public NoAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }
}
