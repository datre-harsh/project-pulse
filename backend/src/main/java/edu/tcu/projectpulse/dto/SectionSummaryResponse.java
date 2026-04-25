package edu.tcu.projectpulse.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record SectionSummaryResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String rubricName,
        List<String> teamNames,
        Set<Integer> activeWeekNumbers
) {
}
