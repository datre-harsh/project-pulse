package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.StudentInvitation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StudentInvitationRepository extends MongoRepository<StudentInvitation, Long> {
    List<StudentInvitation> findBySectionIdAndAcceptedFalseOrderBySentAtDesc(Long sectionId);
}
