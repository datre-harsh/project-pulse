package edu.tcu.projectpulse.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("notifications")
@Getter
@Setter
public class Notification {

    public static final String SEQUENCE_NAME = "notifications_sequence";

    @Id
    private Long id;

    private Long userId;

    private String message;

    private LocalDateTime createdAt;
}
