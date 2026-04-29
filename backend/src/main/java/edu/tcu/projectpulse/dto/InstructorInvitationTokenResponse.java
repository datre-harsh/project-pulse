package edu.tcu.projectpulse.dto;

import java.time.LocalDateTime;

public record InstructorInvitationTokenResponse(
        String email,
        String subject,
        LocalDateTime sentAt,
        boolean accepted
) {
}
