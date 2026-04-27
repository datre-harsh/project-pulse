package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentSectionReport {
    
    private Long studentId;
    
    private String studentName;
    
    private String grade; // Format: "54/60"
    
    private List<EvaluatorDetail> evaluators;
    
    public StudentSectionReport(Long studentId, String studentName, String grade, List<EvaluatorDetail> evaluators) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.grade = grade;
        this.evaluators = evaluators;
    }
}
