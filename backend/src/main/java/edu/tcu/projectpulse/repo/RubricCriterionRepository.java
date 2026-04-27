package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.RubricCriterion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RubricCriterionRepository extends MongoRepository<RubricCriterion, Long> {
    List<RubricCriterion> findByRubricIdOrderByIdAsc(Long rubricId);
}
