package edu.tcu.projectpulse.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("instructor_invitations")
@Getter
@Setter
public class InstructorInvitation {

    public static final String SEQUENCE_NAME = "instructor_invitations_sequence";

    @Id
    private Long id;

    private String email;

    @Indexed(unique = true)
    private String token;

    private String subject;

    private String message;

    private LocalDateTime sentAt;

    private boolean accepted = false;
}
