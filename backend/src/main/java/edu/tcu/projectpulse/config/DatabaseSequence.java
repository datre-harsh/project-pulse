package edu.tcu.projectpulse.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("database_sequences")
@Getter
@Setter
public class DatabaseSequence {

    @Id
    private String id;

    private long seq;
}
