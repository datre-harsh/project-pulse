package edu.tcu.projectpulse.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "peer_evaluations")
public class PeerEvaluation {
    
    public static final String SEQUENCE_NAME = "peer_evaluation_sequence";
    
    @Id
    private Long id;
    
    private Long evaluatorId;
    
    private Long evaluateeId;
    
    private String weekId;
    
    private Map<String, Integer> scores; // Rubric criteria scores
    
    private String publicComment;
    
    private String privateComment;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
