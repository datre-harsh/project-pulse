package edu.tcu.projectpulse.dto;

import edu.tcu.projectpulse.domain.ActivityStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WarActivityRequest(
        @NotNull Long sectionId,
        @NotNull Long teamId,
        @NotNull Long studentId,
        @NotNull @Min(1) @Max(20) Integer weekNumber,
        @NotBlank String category,
        @NotBlank String plannedActivity,
        @NotBlank String description,
        @NotNull @Min(0) Double plannedHours,
        @NotNull @Min(0) Double actualHours,
        @NotNull ActivityStatus status
) {
}
