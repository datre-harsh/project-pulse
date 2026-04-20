package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.WarActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarActivityRepository extends JpaRepository<WarActivity, Long> {
    List<WarActivity> findByTeamIdAndWeekNumberBetweenOrderByWeekNumberAsc(Long teamId, Integer startWeek, Integer endWeek);
    List<WarActivity> findByStudentIdAndWeekNumberBetweenOrderByWeekNumberAsc(Long studentId, Integer startWeek, Integer endWeek);
}
