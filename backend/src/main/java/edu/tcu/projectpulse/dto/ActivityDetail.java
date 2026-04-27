package edu.tcu.projectpulse.dto;

import lombok.Data;

@Data
public class ActivityDetail {
    
    private String category;
    
    private String plannedActivity;
    
    private String description;
    
    private Double plannedHours;
    
    private Double actualHours;
    
    private String status;
    
    public ActivityDetail(String category, String plannedActivity, String description, 
                         Double plannedHours, Double actualHours, String status) {
        this.category = category;
        this.plannedActivity = plannedActivity;
        this.description = description;
        this.plannedHours = plannedHours;
        this.actualHours = actualHours;
        this.status = status;
    }
}
