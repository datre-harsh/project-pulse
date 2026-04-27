package edu.tcu.projectpulse.dto;

import lombok.Data;

@Data
public class StudentEvaluation {
    
    private String evaluatorName;
    
    private String publicComments;
    
    private String privateComments;
    
    public StudentEvaluation(String evaluatorName, String publicComments, String privateComments) {
        this.evaluatorName = evaluatorName;
        this.publicComments = publicComments;
        this.privateComments = privateComments;
    }
}
