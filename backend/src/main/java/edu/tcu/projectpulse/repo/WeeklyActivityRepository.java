package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.WeeklyActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeeklyActivityRepository extends MongoRepository<WeeklyActivity, String> {
    
    List<WeeklyActivity> findByStudentIdAndWeekIdOrderByCreatedAtDesc(String studentId, String weekId);
    
    List<WeeklyActivity> findByStudentIdOrderByCreatedAtDesc(String studentId);
    
    List<WeeklyActivity> findByWeekIdOrderByCreatedAtDesc(String weekId);
}
