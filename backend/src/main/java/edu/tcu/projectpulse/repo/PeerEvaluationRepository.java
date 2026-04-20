package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.PeerEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PeerEvaluationRepository extends JpaRepository<PeerEvaluation, Long> {
    Optional<PeerEvaluation> findByEvaluatorStudentIdAndEvaluateeStudentIdAndTargetWeekNumber(Long evaluatorId, Long evaluateeId, Integer week);
    List<PeerEvaluation> findBySectionIdAndTargetWeekNumberBetween(Long sectionId, Integer startWeek, Integer endWeek);
    List<PeerEvaluation> findByEvaluateeStudentIdAndTargetWeekNumberBetween(Long evaluateeStudentId, Integer startWeek, Integer endWeek);
    List<PeerEvaluation> findByEvaluatorStudentId(Long evaluatorStudentId);
}
