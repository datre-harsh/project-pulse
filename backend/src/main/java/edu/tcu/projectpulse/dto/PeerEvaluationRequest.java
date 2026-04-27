package edu.tcu.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class PeerEvaluationRequest {
    
    @NotNull(message = "Evaluatee ID is required")
    private Long evaluateeId;
    
    @NotBlank(message = "Week ID is required")
    private String weekId;
    
    @NotEmpty(message = "Scores are required")
    private Map<String, Integer> scores;
    
    @Size(max = 1000, message = "Public comment must be less than 1000 characters")
    private String publicComment;
    
    @Size(max = 1000, message = "Private comment must be less than 1000 characters")
    private String privateComment;
}
