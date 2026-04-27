package edu.tcu.projectpulse.dto;

import java.util.List;

public record InstructorDetailResponse(
        Long id,
        String firstName,
        String lastName,
        String status,
        List<InstructorSectionTeamsResponse> supervisedTeams
) {
}
