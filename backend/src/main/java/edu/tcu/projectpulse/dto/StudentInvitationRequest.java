package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentInvitationRequest(
        @NotNull Long sectionId,
        @NotBlank String emails,
        String subject,
        String message
) {
}
