package edu.tcu.projectpulse.dto;

import java.math.BigDecimal;

public record RubricCriterionResponse(
        Long id,
        String name,
        String description,
        BigDecimal maxScore,
        boolean active
) {
}
