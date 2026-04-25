package edu.tcu.projectpulse.dto;

import java.util.List;

public record TeamDetailResponse(
        Long id,
        Long sectionId,
        String sectionName,
        String name,
        String description,
        String websiteUrl,
        List<UserSummaryResponse> students,
        List<UserSummaryResponse> instructors
) {
}
