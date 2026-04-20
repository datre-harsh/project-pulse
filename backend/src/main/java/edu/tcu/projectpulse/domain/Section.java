package edu.tcu.projectpulse.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sections")
@Getter
@Setter
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String semester;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer activeWeekStart;

    @Column(nullable = false)
    private Integer activeWeekEnd;
}
