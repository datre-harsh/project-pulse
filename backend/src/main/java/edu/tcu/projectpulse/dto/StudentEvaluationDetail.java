package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class StudentEvaluationDetail {
    
    private Long evaluationId;
    
    private Long evaluatorId;
    
    private String evaluatorName;
    
    private Long evaluateeId;
    
    private String weekId;
    
    private Map<String, Double> scores;
    
    private String publicComment;
    
    private String privateComment;
    
    private LocalDateTime createdAt;
    
    public StudentEvaluationDetail(Long evaluationId, Long evaluatorId, String evaluatorName, Long evaluateeId, 
                                 String weekId, Map<String, Double> scores, String publicComment, 
                                 String privateComment, LocalDateTime createdAt) {
        this.evaluationId = evaluationId;
        this.evaluatorId = evaluatorId;
        this.evaluatorName = evaluatorName;
        this.evaluateeId = evaluateeId;
        this.weekId = weekId;
        this.scores = scores;
        this.publicComment = publicComment;
        this.privateComment = privateComment;
        this.createdAt = createdAt;
    }
}
