package edu.tcu.projectpulse.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "peer_evaluations")
@Getter
@Setter
public class PeerEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sectionId;

    @Column(nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private Long evaluatorStudentId;

    @Column(nullable = false)
    private Long evaluateeStudentId;

    @Column(nullable = false)
    private Integer targetWeekNumber;

    @Column(nullable = false)
    private Integer totalScore;

    @Column(nullable = false, length = 1000)
    private String publicComment;

    @Column(nullable = false, updatable = false)
    private LocalDate submittedDate;
}
