package edu.tcu.projectpulse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PeerEvaluationRequest(
        @NotNull Long sectionId,
        @NotNull Long teamId,
        @NotNull Long evaluatorStudentId,
        @NotNull Long evaluateeStudentId,
        @NotNull @Min(1) @Max(20) Integer targetWeekNumber,
        @NotBlank String publicComment,
        @NotEmpty List<@Valid CriterionScoreRequest> scores
) {
}
