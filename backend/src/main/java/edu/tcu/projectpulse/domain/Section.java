package edu.tcu.projectpulse.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Document("sections")
@Getter
@Setter
public class Section {

    public static final String SEQUENCE_NAME = "sections_sequence";

    @Id
    private Long id;

    @Indexed(unique = true)
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long rubricId;

    private Set<Integer> inactiveWeekNumbers = new HashSet<>();

    private Set<Long> studentIds = new HashSet<>();

    private Set<Long> instructorIds = new HashSet<>();
}
