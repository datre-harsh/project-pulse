package edu.tcu.projectpulse.dto;

import java.time.LocalDateTime;

public record StudentInvitationResponse(
        Long id,
        Long sectionId,
        String email,
        String subject,
        String message,
        String token,
        LocalDateTime sentAt,
        boolean accepted
) {
}
