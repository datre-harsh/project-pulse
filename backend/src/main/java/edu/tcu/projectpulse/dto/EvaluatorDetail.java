package edu.tcu.projectpulse.dto;

import lombok.Data;

@Data
public class EvaluatorDetail {
    
    private String evaluatorName;
    
    private String publicComment;
    
    private String privateComment;
    
    public EvaluatorDetail(String evaluatorName, String publicComment, String privateComment) {
        this.evaluatorName = evaluatorName;
        this.publicComment = publicComment;
        this.privateComment = privateComment;
    }
}
