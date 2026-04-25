package edu.tcu.projectpulse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RubricRequest(
        @NotBlank String name,
        @Valid @NotEmpty List<RubricCriterionRequest> criteria
) {
}
