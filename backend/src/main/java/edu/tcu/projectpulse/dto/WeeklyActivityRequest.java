package edu.tcu.projectpulse.dto;

import edu.tcu.projectpulse.domain.ActivityCategory;
import edu.tcu.projectpulse.domain.ActivityStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WeeklyActivityRequest {
    
    @NotNull(message = "Category is required")
    private ActivityCategory category;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Planned hours is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Planned hours must be positive")
    private Double plannedHours;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Actual hours cannot be negative")
    private Double actualHours;
    
    @NotNull(message = "Status is required")
    private ActivityStatus status;
    
    @NotBlank(message = "Week ID is required")
    private String weekId;
}
