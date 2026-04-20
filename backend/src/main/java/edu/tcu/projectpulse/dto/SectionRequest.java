package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionRequest(
        @NotBlank String name,
        @NotBlank String semester,
        @NotNull Integer year,
        @NotNull @Min(1) @Max(20) Integer activeWeekStart,
        @NotNull @Min(1) @Max(20) Integer activeWeekEnd
) {
}
