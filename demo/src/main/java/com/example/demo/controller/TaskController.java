package com.example.demo.controller;

import com.example.demo.dto.TaskDefinitionResponse;
import com.example.demo.dto.TaskTrialResponse;
import com.example.demo.service.TaskCatalogService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(originPatterns = "*")
/**
 * Exposes the task catalog used by the frontend session flow.
 */
public class TaskController {

    private final TaskCatalogService taskCatalogService;

    public TaskController(TaskCatalogService taskCatalogService) {
        this.taskCatalogService = taskCatalogService;
    }

    @GetMapping
    /**
     * Returns the available task definitions.
     *
     * @return task catalog
     */
    public ResponseEntity<List<TaskDefinitionResponse>> getTasks() {
        return ResponseEntity.ok(taskCatalogService.getTasks());
    }

    @GetMapping("/{taskId}")
    /**
     * Returns a single task definition.
     *
     * @param taskId task identifier
     * @return matching task definition
     */
    public ResponseEntity<TaskDefinitionResponse> getTask(@PathVariable String taskId) {
        return ResponseEntity.ok(taskCatalogService.getTaskById(taskId));
    }

    @GetMapping("/{taskId}/trials")
    /**
     * Returns the trial templates for a task.
     *
     * @param taskId task identifier
     * @return ordered trial templates
     */
    public ResponseEntity<List<TaskTrialResponse>> getTaskTrials(@PathVariable String taskId) {
        return ResponseEntity.ok(taskCatalogService.getTaskTrials(taskId));
    }
}
