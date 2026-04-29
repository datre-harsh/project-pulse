package edu.tcu.projectpulse.dto;

import edu.tcu.projectpulse.domain.Role;

public record LoginResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role
) {
}
