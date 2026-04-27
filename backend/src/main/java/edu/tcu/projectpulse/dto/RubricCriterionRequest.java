package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RubricCriterionRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull BigDecimal maxScore,
        Boolean active
) {
}
