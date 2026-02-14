package com.taskmanager.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskmanager.api.entity.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {

}
