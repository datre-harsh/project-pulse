package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.PeerEvaluation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeerEvaluationRepository extends MongoRepository<PeerEvaluation, Long> {
    
    Optional<PeerEvaluation> findByEvaluatorIdAndEvaluateeIdAndWeekId(Long evaluatorId, Long evaluateeId, String weekId);
    
    List<PeerEvaluation> findByEvaluatorIdAndWeekId(Long evaluatorId, String weekId);
    
    List<PeerEvaluation> findByEvaluateeIdAndWeekId(Long evaluateeId, String weekId);

    List<PeerEvaluation> findByEvaluateeIdAndWeekIdOrderByCreatedAtDesc(Long evaluateeId, String weekId);
    
    List<PeerEvaluation> findByWeekId(String weekId);
}
