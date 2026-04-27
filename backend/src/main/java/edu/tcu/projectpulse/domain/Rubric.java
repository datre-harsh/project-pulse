package edu.tcu.projectpulse.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("rubrics")
@Getter
@Setter
public class Rubric {

    public static final String SEQUENCE_NAME = "rubrics_sequence";

    @Id
    private Long id;

    @Indexed(unique = true)
    private String name;
}
