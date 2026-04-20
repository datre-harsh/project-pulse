package edu.tcu.projectpulse.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sectionId;

    @Column(nullable = false, unique = true)
    private String name;

    @ElementCollection
    @CollectionTable(name = "team_students", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "student_id")
    private Set<Long> studentIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "team_instructors", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "instructor_id")
    private Set<Long> instructorIds = new HashSet<>();
}
