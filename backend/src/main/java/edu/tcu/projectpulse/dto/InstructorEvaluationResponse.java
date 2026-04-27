package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InstructorEvaluationResponse {
    
    private Long studentId;
    
    private String studentName;
    
    private String weekId;
    
    private List<StudentEvaluationDetail> evaluations;
    
    private List<String> nonEvaluators; // Students who did not submit evaluations
    
    private Double systemSuggestedGrade;
    
    private String message;
    
    public InstructorEvaluationResponse(Long studentId, String studentName, String weekId, 
                                      List<StudentEvaluationDetail> evaluations, 
                                      List<String> nonEvaluators, 
                                      Double systemSuggestedGrade, 
                                      String message) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.weekId = weekId;
        this.evaluations = evaluations;
        this.nonEvaluators = nonEvaluators;
        this.systemSuggestedGrade = systemSuggestedGrade;
        this.message = message;
    }
}
