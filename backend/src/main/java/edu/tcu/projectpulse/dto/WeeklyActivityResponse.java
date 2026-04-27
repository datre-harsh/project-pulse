package edu.tcu.projectpulse.dto;

import edu.tcu.projectpulse.domain.ActivityCategory;
import edu.tcu.projectpulse.domain.ActivityStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WeeklyActivityResponse {
    
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
    
    public WeeklyActivityResponse(String id, String studentId, ActivityCategory category, String description, 
                                 Double plannedHours, Double actualHours, ActivityStatus status, 
                                 String weekId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.studentId = studentId;
        this.category = category;
        this.description = description;
        this.plannedHours = plannedHours;
        this.actualHours = actualHours;
        this.status = status;
        this.weekId = weekId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
