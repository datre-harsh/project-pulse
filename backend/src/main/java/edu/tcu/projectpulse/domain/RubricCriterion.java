package edu.tcu.projectpulse.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("rubric_criteria")
@Getter
@Setter
public class RubricCriterion {

    public static final String SEQUENCE_NAME = "rubric_criteria_sequence";

    @Id
    private Long id;

    private Long rubricId;

    private String name;

    private String description;

    private BigDecimal maxScore;

    private boolean active = true;
}
