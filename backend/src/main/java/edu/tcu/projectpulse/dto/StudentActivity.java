package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentActivity {
    
    private String name;
    
    private List<ActivityDetail> activities;
    
    public StudentActivity(String name, List<ActivityDetail> activities) {
        this.name = name;
        this.activities = activities;
    }
}
