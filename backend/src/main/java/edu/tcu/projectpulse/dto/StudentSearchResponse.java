package edu.tcu.projectpulse.dto;

public record StudentSearchResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Long sectionId,
        String sectionName,
        Long teamId,
        String teamName
) {
}
