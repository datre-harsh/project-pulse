package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PeerEvaluationResponse {
    
    private Long id;
    
    private Long evaluatorId;
    
    private Long evaluateeId;
    
    private String weekId;
    
    private Map<String, Integer> scores;
    
    private String publicComment;
    
    private String privateComment;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String message;
    
    public PeerEvaluationResponse(Long id, Long evaluatorId, Long evaluateeId, String weekId, 
                                 Map<String, Integer> scores, String publicComment, String privateComment,
                                 LocalDateTime createdAt, LocalDateTime updatedAt, String message) {
        this.id = id;
        this.evaluatorId = evaluatorId;
        this.evaluateeId = evaluateeId;
        this.weekId = weekId;
        this.scores = scores;
        this.publicComment = publicComment;
        this.privateComment = privateComment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.message = message;
    }
}
