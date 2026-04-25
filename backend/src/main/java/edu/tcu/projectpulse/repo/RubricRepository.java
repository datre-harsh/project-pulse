package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.Rubric;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RubricRepository extends MongoRepository<Rubric, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Rubric> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
