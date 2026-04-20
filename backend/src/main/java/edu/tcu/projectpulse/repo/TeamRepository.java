package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findBySectionId(Long sectionId);
}
