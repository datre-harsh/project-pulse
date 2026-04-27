package edu.tcu.projectpulse.dto;

import lombok.Data;

import java.util.List;

@Data
public class SectionEvaluationReportResponse {
    
    private Long sectionId;
    
    private String sectionName;
    
    private String weekId;
    
    private List<StudentSectionReport> studentReports;
    
    private String message;
    
    public SectionEvaluationReportResponse(Long sectionId, String sectionName, String weekId, 
                                         List<StudentSectionReport> studentReports, String message) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.weekId = weekId;
        this.studentReports = studentReports;
        this.message = message;
    }
}
