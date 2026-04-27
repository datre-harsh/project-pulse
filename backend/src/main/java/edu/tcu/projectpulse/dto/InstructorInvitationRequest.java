package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;

public record InstructorInvitationRequest(
        @NotBlank String emails,
        String subject,
        String message
) {
}
