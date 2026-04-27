package edu.tcu.projectpulse.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "weekly_activities")
public class WeeklyActivity {
    
    public static final String SEQUENCE_NAME = "weekly_activity_sequence";
    
    @Id
    private String id;
    
    private String studentId;
    
    private ActivityCategory category;
    
    private String description;
    
    private Double plannedHours;
    
    private Double actualHours;
    
    private ActivityStatus status;
    
    private String weekId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
