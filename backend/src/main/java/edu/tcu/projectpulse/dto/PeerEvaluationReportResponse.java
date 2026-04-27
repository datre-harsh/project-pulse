package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PeerEvaluationReportResponse {
    
    private Long studentId;
    
    private String weekId;
    
    private Map<String, Double> averageScores; // Aggregated scores per criterion
    
    private List<String> publicComments; // Anonymous public comments only
    
    private String message;
    
    public PeerEvaluationReportResponse(Long studentId, String weekId, Map<String, Double> averageScores, 
                                       List<String> publicComments, String message) {
        this.studentId = studentId;
        this.weekId = weekId;
        this.averageScores = averageScores;
        this.publicComments = publicComments;
        this.message = message;
    }
}
