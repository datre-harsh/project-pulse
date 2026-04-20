package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.PeerEvaluationCriterionScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeerEvaluationCriterionScoreRepository extends JpaRepository<PeerEvaluationCriterionScore, Long> {
    List<PeerEvaluationCriterionScore> findByEvaluationId(Long evaluationId);
}
