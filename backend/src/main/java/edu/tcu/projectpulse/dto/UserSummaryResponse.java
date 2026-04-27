package edu.tcu.projectpulse.dto;

import edu.tcu.projectpulse.domain.Role;

public record UserSummaryResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        boolean active
) {
}
