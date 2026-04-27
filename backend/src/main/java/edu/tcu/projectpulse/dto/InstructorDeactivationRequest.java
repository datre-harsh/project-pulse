package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;

public record InstructorDeactivationRequest(
        @NotBlank(message = "A deactivation reason is required")
        String reason
) {
}
