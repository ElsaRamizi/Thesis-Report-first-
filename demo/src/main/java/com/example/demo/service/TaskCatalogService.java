package com.example.demo.service;

import com.example.demo.dto.TaskDefinitionResponse;
import com.example.demo.dto.TaskTrialResponse;
import com.example.demo.exception.BadRequestException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
/**
 * Provides the currently available cognitive task definitions and trial templates.
 */
public class TaskCatalogService {

    private final List<TaskDefinitionResponse> tasks = List.of(
        new TaskDefinitionResponse(
            "memory-span",
            "Memory Span",
            "Track short sequences under time pressure to reflect working-memory stability.",
            6,
            "Progressive",
            List.of("Recall accuracy", "Reaction time", "Error rate")
        )
    );

    private final Map<String, List<TaskTrialResponse>> trialsByTask = Map.of(
        "memory-span",
        List.of(
            new TaskTrialResponse("Select the sequence you just saw.", "4 - 1 - 7", null, List.of("4 - 1 - 7", "4 - 7 - 1", "1 - 4 - 7"), "4 - 1 - 7"),
            new TaskTrialResponse("Select the sequence you just saw.", "8 - 2 - 5 - 9", null, List.of("8 - 5 - 2 - 9", "8 - 2 - 5 - 9", "2 - 8 - 5 - 9"), "8 - 2 - 5 - 9"),
            new TaskTrialResponse("Select the sequence you just saw.", "3 - 6 - 1 - 4", null, List.of("3 - 1 - 6 - 4", "3 - 6 - 1 - 4", "6 - 3 - 1 - 4"), "3 - 6 - 1 - 4"),
            new TaskTrialResponse("Select the sequence you just saw.", "9 - 4 - 2 - 7 - 1", null, List.of("9 - 4 - 2 - 7 - 1", "9 - 2 - 4 - 7 - 1", "4 - 9 - 2 - 7 - 1"), "9 - 4 - 2 - 7 - 1"),
            new TaskTrialResponse("Select the sequence you just saw.", "5 - 8 - 3 - 6 - 2", null, List.of("5 - 8 - 6 - 3 - 2", "8 - 5 - 3 - 6 - 2", "5 - 8 - 3 - 6 - 2"), "5 - 8 - 3 - 6 - 2")
        )
    );

    /**
     * Returns the full list of available task definitions.
     *
     * @return task catalog
     */
    public List<TaskDefinitionResponse> getTasks() {
        return tasks;
    }

    /**
     * Loads a single task definition by identifier.
     *
     * @param taskId task identifier
     * @return matching task definition
     */
    public TaskDefinitionResponse getTaskById(String taskId) {
        return tasks.stream()
            .filter(task -> task.getId().equals(taskId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Task was not found."));
    }

    /**
     * Returns the trial templates associated with a task.
     *
     * @param taskId task identifier
     * @return ordered list of trial templates
     */
    public List<TaskTrialResponse> getTaskTrials(String taskId) {
        if (!trialsByTask.containsKey(taskId)) {
            throw new BadRequestException("Task trials were not found.");
        }

        return trialsByTask.get(taskId);
    }
}
