package edu.tcu.projectpulse.config;

import edu.tcu.projectpulse.domain.Role;
import edu.tcu.projectpulse.domain.Rubric;
import edu.tcu.projectpulse.domain.RubricCriterion;
import edu.tcu.projectpulse.domain.UserAccount;
import edu.tcu.projectpulse.repo.RubricCriterionRepository;
import edu.tcu.projectpulse.repo.RubricRepository;
import edu.tcu.projectpulse.repo.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userRepo;
    private final RubricRepository rubricRepo;
    private final RubricCriterionRepository rubricCriterionRepo;
    private final SequenceGeneratorService sequenceGeneratorService;

    public DataInitializer(
            UserAccountRepository userRepo,
            RubricRepository rubricRepo,
            RubricCriterionRepository rubricCriterionRepo,
            SequenceGeneratorService sequenceGeneratorService
    ) {
        this.userRepo = userRepo;
        this.rubricRepo = rubricRepo;
        this.rubricCriterionRepo = rubricCriterionRepo;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedRubric();
    }

    private void seedUsers() {
        createUserIfMissing("admin@projectpulse.local", "Admin", "User", Role.ADMIN);
        createUserIfMissing("instructor@projectpulse.local", "Ingrid", "Instructor", Role.INSTRUCTOR);
        createUserIfMissing("student1@projectpulse.local", "Sam", "Student", Role.STUDENT);
        createUserIfMissing("student2@projectpulse.local", "Taylor", "Student", Role.STUDENT);
    }

    private void createUserIfMissing(String email, String firstName, String lastName, Role role) {
        if (userRepo.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setId(sequenceGeneratorService.generateSequence(UserAccount.SEQUENCE_NAME));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setActive(true);
        userRepo.save(user);
    }

    private void seedRubric() {
        Rubric rubric = rubricRepo.findByNameContainingIgnoreCaseOrderByNameAsc("Peer Evaluation Rubric v1").stream()
                .filter(existing -> existing.getName().equalsIgnoreCase("Peer Evaluation Rubric v1"))
                .findFirst()
                .orElseGet(() -> {
                    Rubric created = new Rubric();
                    created.setId(sequenceGeneratorService.generateSequence(Rubric.SEQUENCE_NAME));
                    created.setName("Peer Evaluation Rubric v1");
                    return rubricRepo.save(created);
                });

        if (!rubricCriterionRepo.findByRubricIdOrderByIdAsc(rubric.getId()).isEmpty()) {
            return;
        }

        createCriterion(rubric.getId(), "Quality of work", "How do you rate the quality of this teammate's work?", BigDecimal.TEN);
        createCriterion(rubric.getId(), "Productivity", "How productive is this teammate?", BigDecimal.TEN);
        createCriterion(rubric.getId(), "Initiative", "How proactive is this teammate?", BigDecimal.TEN);
    }

    private void createCriterion(Long rubricId, String name, String description, BigDecimal maxScore) {
        RubricCriterion criterion = new RubricCriterion();
        criterion.setId(sequenceGeneratorService.generateSequence(RubricCriterion.SEQUENCE_NAME));
        criterion.setRubricId(rubricId);
        criterion.setName(name);
        criterion.setDescription(description);
        criterion.setMaxScore(maxScore);
        criterion.setActive(true);
        rubricCriterionRepo.save(criterion);
    }
}
