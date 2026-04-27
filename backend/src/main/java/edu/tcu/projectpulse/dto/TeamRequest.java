package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record TeamRequest(
        @NotNull Long sectionId,
        @NotBlank String name,
        @NotBlank String description,
        String websiteUrl,
        Set<Long> studentIds,
        Set<Long> instructorIds
) {
}
