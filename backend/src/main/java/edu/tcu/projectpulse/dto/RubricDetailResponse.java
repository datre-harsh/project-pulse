package edu.tcu.projectpulse.dto;

import java.util.List;

public record RubricDetailResponse(
        Long id,
        String name,
        List<RubricCriterionResponse> criteria
) {
}
