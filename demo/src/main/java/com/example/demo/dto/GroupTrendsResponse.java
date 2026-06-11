package com.example.demo.dto;

import java.util.List;

public record GroupTrendsResponse(
    String taskFilter,
    List<GroupParticipantTrendRow> participants,
    List<GroupTimelinePointDto> groupTimeline
) {}
