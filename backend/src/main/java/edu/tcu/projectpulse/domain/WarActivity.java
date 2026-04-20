package edu.tcu.projectpulse.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "war_activities")
@Getter
@Setter
public class WarActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sectionId;

    @Column(nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String plannedActivity;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private Double plannedHours;

    @Column(nullable = false)
    private Double actualHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;
}
