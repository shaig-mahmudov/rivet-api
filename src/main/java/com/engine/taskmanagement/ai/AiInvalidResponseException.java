package com.engine.taskmanagement.ai;

public class AiInvalidResponseException extends RuntimeException {

    public AiInvalidResponseException(String message) {
        super(message);
    }
}
