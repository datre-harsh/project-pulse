package edu.tcu.projectpulse.dto;

import lombok.Data;

@Data
public class InstructorRegistrationResponse {
    
    private Long instructorId;
    
    private String firstName;
    
    private String lastName;
    
    private String email;
    
    private String message;
    
    public InstructorRegistrationResponse(Long instructorId, String firstName, String lastName, String email, String message) {
        this.instructorId = instructorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.message = message;
    }
}
