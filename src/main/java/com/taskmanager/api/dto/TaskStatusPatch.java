package com.taskmanager.api.dto;

import com.taskmanager.api.entity.TaskStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

// Enables a quick status update
public class TaskStatusPatch {
    private TaskStatus status;
}
