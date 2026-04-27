package edu.tcu.projectpulse.dto;

import lombok.Data;

@Data
public class ProfileUpdateResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String message;
    
    public ProfileUpdateResponse(String id, String firstName, String lastName, String email, String message) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.message = message;
    }
}
