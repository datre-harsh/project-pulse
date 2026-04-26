package edu.tcu.projectpulse.dto;

import java.util.List;

public record InstructorSectionTeamsResponse(
        Long sectionId,
        String sectionName,
        List<String> teamNames
) {
}
