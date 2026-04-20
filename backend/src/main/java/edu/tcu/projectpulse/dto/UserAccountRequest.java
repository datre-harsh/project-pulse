package edu.tcu.projectpulse.dto;

import edu.tcu.projectpulse.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserAccountRequest(
        @Email @NotBlank String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Role role,
        Boolean active
) {
}
