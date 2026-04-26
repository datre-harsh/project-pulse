package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.InstructorInvitation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InstructorInvitationRepository extends MongoRepository<InstructorInvitation, Long> {
    List<InstructorInvitation> findByAcceptedFalseOrderBySentAtDesc();
}
