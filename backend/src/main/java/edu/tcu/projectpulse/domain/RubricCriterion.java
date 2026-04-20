package edu.tcu.projectpulse.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rubric_criteria")
@Getter
@Setter
public class RubricCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer maxScore;

    @Column(nullable = false)
    private boolean active = true;
}
