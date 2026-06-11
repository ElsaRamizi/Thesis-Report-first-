package com.example.demo.dto;

import java.time.LocalDate;

public record ParticipantProfileUpdateRequest(
    LocalDate dateOfBirth,
    String notes,
    Long assignedClinicianId,
    Boolean clearAssignedClinician
) {}
