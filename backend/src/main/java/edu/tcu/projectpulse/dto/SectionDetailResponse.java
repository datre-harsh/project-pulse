package edu.tcu.projectpulse.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record SectionDetailResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        RubricDetailResponse rubric,
        Set<Integer> activeWeekNumbers,
        Set<Integer> inactiveWeekNumbers,
        List<TeamSummaryResponse> teams,
        List<UserSummaryResponse> unassignedStudents,
        List<UserSummaryResponse> unassignedInstructors,
        List<StudentInvitationResponse> pendingInvitations
) {
}
