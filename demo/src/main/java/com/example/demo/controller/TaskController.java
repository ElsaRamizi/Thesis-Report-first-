package com.example.demo.controller;

import com.example.demo.dto.TaskDefinitionResponse;
import com.example.demo.dto.TaskTrialResponse;
import com.example.demo.service.TaskCatalogService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskCatalogService taskCatalogService;

    public TaskController(TaskCatalogService taskCatalogService) {
        this.taskCatalogService = taskCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<TaskDefinitionResponse>> getTasks() {
        return ResponseEntity.ok(taskCatalogService.getTasks());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDefinitionResponse> getTask(@PathVariable String taskId) {
        return ResponseEntity.ok(taskCatalogService.getTaskById(taskId));
    }

    @GetMapping("/{taskId}/trials")
    public ResponseEntity<List<TaskTrialResponse>> getTaskTrials(@PathVariable String taskId) {
        return ResponseEntity.ok(taskCatalogService.getTaskTrials(taskId));
    }
}
