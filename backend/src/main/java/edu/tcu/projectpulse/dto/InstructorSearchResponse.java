package edu.tcu.projectpulse.dto;

public record InstructorSearchResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Integer academicYear,
        Long sectionId,
        String sectionName,
        Long teamId,
        String teamName,
        String status
) {
}
