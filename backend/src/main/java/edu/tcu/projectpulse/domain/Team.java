package edu.tcu.projectpulse.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document("teams")
@Getter
@Setter
public class Team {

    public static final String SEQUENCE_NAME = "teams_sequence";

    @Id
    private Long id;

    private Long sectionId;

    @Indexed(unique = true)
    private String name;

    private String description;

    private String websiteUrl;

    private Set<Long> studentIds = new HashSet<>();

    private Set<Long> instructorIds = new HashSet<>();
}
