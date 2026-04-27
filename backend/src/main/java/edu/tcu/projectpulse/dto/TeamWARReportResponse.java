package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeamWARReportResponse {
    
    private Long teamId;
    
    private String teamName;
    
    private String weekId;
    
    private List<StudentActivity> activeStudents;
    
    private List<String> missingStudents;
    
    private String message;
    
    public TeamWARReportResponse(Long teamId, String teamName, String weekId, 
                               List<StudentActivity> activeStudents, 
                               List<String> missingStudents, 
                               String message) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.weekId = weekId;
        this.activeStudents = activeStudents;
        this.missingStudents = missingStudents;
        this.message = message;
    }
}
