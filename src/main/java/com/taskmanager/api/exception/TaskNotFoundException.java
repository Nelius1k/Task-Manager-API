package com.taskmanager.api.exception;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(UUID id) {
        super("The task " + id + " cannot be found");
    }
}
