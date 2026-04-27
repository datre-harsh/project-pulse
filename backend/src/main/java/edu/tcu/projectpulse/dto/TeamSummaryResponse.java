package edu.tcu.projectpulse.dto;

import java.util.List;

public record TeamSummaryResponse(
        Long id,
        Long sectionId,
        String sectionName,
        String name,
        String description,
        String websiteUrl,
        List<String> studentNames,
        List<String> instructorNames
) {
}
