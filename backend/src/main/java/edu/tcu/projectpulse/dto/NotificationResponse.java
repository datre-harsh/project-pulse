package edu.tcu.projectpulse.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String message,
        LocalDateTime createdAt
) {
}
