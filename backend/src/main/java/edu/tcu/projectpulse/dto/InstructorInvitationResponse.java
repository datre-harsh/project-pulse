package edu.tcu.projectpulse.dto;

import java.time.LocalDateTime;

public record InstructorInvitationResponse(
        Long id,
        String email,
        String subject,
        String message,
        String token,
        LocalDateTime sentAt,
        boolean accepted
) {
}
