package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.Section;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SectionRepository extends MongoRepository<Section, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Section> findByNameContainingIgnoreCaseOrderByNameDesc(String name);
    List<Section> findAllByOrderByNameDesc();
}
