package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TeamRepository extends MongoRepository<Team, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Team> findBySectionId(Long sectionId);
    List<Team> findBySectionIdOrderByNameAsc(Long sectionId);
}
