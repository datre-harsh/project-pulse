package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.NotNull;

public record CriterionScoreRequest(
        @NotNull Long criterionId,
        @NotNull Integer score
) {
}
