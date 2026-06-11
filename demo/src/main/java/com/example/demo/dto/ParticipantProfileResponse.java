package com.example.demo.dto;

import java.time.LocalDate;

public record ParticipantProfileResponse(
    Long participantId,
    String email,
    LocalDate dateOfBirth,
    Integer age,
    String notes,
    Long assignedClinicianId,
    String assignedClinicianEmail
) {}
