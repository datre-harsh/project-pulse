package edu.tcu.projectpulse.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("student_invitations")
@Getter
@Setter
public class StudentInvitation {

    public static final String SEQUENCE_NAME = "student_invitations_sequence";

    @Id
    private Long id;

    private Long sectionId;

    private String email;

    @Indexed(unique = true)
    private String token;

    private String subject;

    private String message;

    private LocalDateTime sentAt;

    private boolean accepted = false;
}
