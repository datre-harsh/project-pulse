package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WarReportRequest(
        @NotNull Long targetId,
        @NotNull @Min(1) @Max(20) Integer startWeek,
        @NotNull @Min(1) @Max(20) Integer endWeek,
        @NotNull Boolean byTeam
) {
}
