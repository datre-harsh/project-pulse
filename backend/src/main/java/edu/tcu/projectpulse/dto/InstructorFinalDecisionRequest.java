package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InstructorFinalDecisionRequest {
    
    @NotNull(message = "Final grade is required")
    private Double finalGrade;
    
    private String instructorComment;
    
    public InstructorFinalDecisionRequest(Double finalGrade, String instructorComment) {
        this.finalGrade = finalGrade;
        this.instructorComment = instructorComment;
    }
}
