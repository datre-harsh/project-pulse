package edu.tcu.projectpulse.dto;

import java.util.List;

public record StudentDetailResponse(
        Long id,
        String firstName,
        String lastName,
        String sectionName,
        String teamName,
        List<String> peerEvaluations,
        List<String> wars
) {
}
