package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record SectionRequest(
        @NotBlank String name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull Long rubricId,
        Set<Long> studentIds,
        Set<Long> instructorIds,
        Set<Integer> inactiveWeekNumbers
) {
}
