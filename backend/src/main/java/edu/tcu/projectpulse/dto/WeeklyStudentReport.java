package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.util.List;

@Data
public class WeeklyStudentReport {
    
    private String weekRange;
    
    private String overallGrade;
    
    private List<StudentEvaluation> evaluations;
    
    public WeeklyStudentReport(String weekRange, String overallGrade, List<StudentEvaluation> evaluations) {
        this.weekRange = weekRange;
        this.overallGrade = overallGrade;
        this.evaluations = evaluations;
    }
}
