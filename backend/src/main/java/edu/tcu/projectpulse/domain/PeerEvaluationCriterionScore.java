package edu.tcu.projectpulse.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "peer_evaluation_criterion_scores")
@Getter
@Setter
public class PeerEvaluationCriterionScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long evaluationId;

    @Column(nullable = false)
    private Long criterionId;

    @Column(nullable = false)
    private Integer score;
}
