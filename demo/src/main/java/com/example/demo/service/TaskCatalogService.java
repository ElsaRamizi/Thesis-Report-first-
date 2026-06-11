package com.example.demo.service;

import com.example.demo.dto.TaskDefinitionResponse;
import com.example.demo.dto.TaskTrialResponse;
import com.example.demo.exception.BadRequestException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TaskCatalogService {

    // hardcoded list of the three thesis tasks
    private final List<TaskDefinitionResponse> tasks = List.of(
        new TaskDefinitionResponse(
            "memory-span",
            "Memory Span",
            "Remember digit sequences. Span increases when you get it right.",
            5,
            "Progressive",
            List.of("Recall", "Span level", "Reaction time")
        ),
        new TaskDefinitionResponse(
            "stroop",
            "Stroop Color-Word",
            "Pick the ink colour, not the word text.",
            4,
            "Fixed",
            List.of("Accuracy", "Reaction time", "Congruent vs incongruent")
        ),
        new TaskDefinitionResponse(
            "dual-n-back",
            "Adaptive Dual N-Back",
            "Match position and letter from N steps back. N goes up or down based on performance.",
            8,
            "Adaptive",
            List.of("Accuracy", "False alarms", "N level")
        )
    );

    private final Map<String, List<TaskTrialResponse>> trialsByTask = Map.of(
        "memory-span",
        List.of(
            new TaskTrialResponse("Trials are generated in the browser.", "engine", null, List.of(), "Adaptive")
        ),
        "stroop",
        List.of(
            new TaskTrialResponse("Trials are generated in the browser.", "engine", null, List.of(), "Fixed")
        ),
        "dual-n-back",
        List.of(
            new TaskTrialResponse("Trials are generated in the browser.", "block-1", null, List.of("Position", "Letter"), "Adaptive"),
            new TaskTrialResponse("Trials are generated in the browser.", "block-2", null, List.of("Position", "Letter"), "Adaptive")
        )
    );

    /// all 3 tasks for task selection page
    public List<TaskDefinitionResponse> getTasks() {
        return tasks;
    }

    /// one task metadata by id (stroop, memory-span, dual-n-back)
    public TaskDefinitionResponse getTaskById(String taskId) {
        return tasks.stream()
            .filter(task -> task.getId().equals(taskId))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Unknown task."));
    }

    /// placeholder trials — real trials are generated in React for stroop/span/nback
    public List<TaskTrialResponse> getTaskTrials(String taskId) {
        if (!trialsByTask.containsKey(taskId)) {
            throw new BadRequestException("Task trials were not found.");
        }

        return trialsByTask.get(taskId);
    }
}
