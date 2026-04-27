package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.util.List;

@Data
public class WeeklyStudentWARReport {
    
    private String weekRange;
    
    private List<ActivityDetail> activities;
    
    public WeeklyStudentWARReport(String weekRange, List<ActivityDetail> activities) {
        this.weekRange = weekRange;
        this.activities = activities;
    }
}
