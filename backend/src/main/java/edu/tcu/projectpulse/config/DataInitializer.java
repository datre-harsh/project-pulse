package edu.tcu.projectpulse.config;

import edu.tcu.projectpulse.domain.Role;
import edu.tcu.projectpulse.domain.Rubric;
import edu.tcu.projectpulse.domain.RubricCriterion;
import edu.tcu.projectpulse.domain.Section;
import edu.tcu.projectpulse.domain.Team;
import edu.tcu.projectpulse.domain.UserAccount;
import edu.tcu.projectpulse.repo.RubricCriterionRepository;
import edu.tcu.projectpulse.repo.RubricRepository;
import edu.tcu.projectpulse.repo.SectionRepository;
import edu.tcu.projectpulse.repo.TeamRepository;
import edu.tcu.projectpulse.repo.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userRepo;
    private final RubricRepository rubricRepo;
    private final RubricCriterionRepository rubricCriterionRepo;
    private final SectionRepository sectionRepo;
    private final TeamRepository teamRepo;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(
            UserAccountRepository userRepo,
            RubricRepository rubricRepo,
            RubricCriterionRepository rubricCriterionRepo,
            SectionRepository sectionRepo,
            TeamRepository teamRepo,
            SequenceGeneratorService sequenceGeneratorService
    ) {
        this.userRepo = userRepo;
        this.rubricRepo = rubricRepo;
        this.rubricCriterionRepo = rubricCriterionRepo;
        this.sectionRepo = sectionRepo;
        this.teamRepo = teamRepo;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        Rubric rubric = seedRubric();
        seedDemoTeam(rubric);
    }

    private void seedUsers() {
        createUserIfMissing("admin@projectpulse.local", "Admin", "User", Role.ADMIN);
        createUserIfMissing("instructor@projectpulse.local", "Ingrid", "Instructor", Role.INSTRUCTOR);
        createUserIfMissing("alex.instructor@projectpulse.local", "Alex", "Instructor", Role.INSTRUCTOR);
        createUserIfMissing("blair.instructor@projectpulse.local", "Blair", "Instructor", Role.INSTRUCTOR);
        createUserIfMissing("casey.instructor@projectpulse.local", "Casey", "Instructor", Role.INSTRUCTOR);
        createUserIfMissing("deactivated.instructor@projectpulse.local", "Deactivated", "Instructor", Role.INSTRUCTOR, false);
        createUserIfMissing("student1@projectpulse.local", "Sam", "Student", Role.STUDENT);
        createUserIfMissing("student2@projectpulse.local", "Taylor", "Student", Role.STUDENT);
        createUserIfMissing("harsh.mehta@tcu.edu", "Harsh", "Mehta", Role.STUDENT);
        createUserIfMissing("ralph.nguyen@tcu.edu", "Ralph", "Nguyen", Role.STUDENT);
        createUserIfMissing("jenny.patel@tcu.edu", "Jenny", "Patel", Role.STUDENT);
    }

    private void createUserIfMissing(String email, String firstName, String lastName, Role role) {
        createUserIfMissing(email, firstName, lastName, role, true);
    }

    private void createUserIfMissing(String email, String firstName, String lastName, Role role, boolean active) {
        var existing = userRepo.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            UserAccount user = existing.get();
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode("password"));
                userRepo.save(user);
            }
            return;
        }
        UserAccount user = new UserAccount();
        user.setId(sequenceGeneratorService.generateSequence(UserAccount.SEQUENCE_NAME));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setActive(active);
        user.setPassword(passwordEncoder.encode("password"));
        userRepo.save(user);
    }

    private Rubric seedRubric() {
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
            return rubric;
        }

        createCriterion(rubric.getId(), "Quality of work", "How do you rate the quality of this teammate's work?", BigDecimal.TEN);
        createCriterion(rubric.getId(), "Productivity", "How productive is this teammate?", BigDecimal.TEN);
        createCriterion(rubric.getId(), "Initiative", "How proactive is this teammate?", BigDecimal.TEN);
        return rubric;
    }

    private void seedDemoTeam(Rubric rubric) {
        UserAccount instructor = userRepo.findByEmailIgnoreCase("instructor@projectpulse.local").orElse(null);
        UserAccount sam = userRepo.findByEmailIgnoreCase("student1@projectpulse.local").orElse(null);
        UserAccount taylor = userRepo.findByEmailIgnoreCase("student2@projectpulse.local").orElse(null);

        if (instructor == null || sam == null || taylor == null) {
            return;
        }

        Section section = sectionRepo.findAllByOrderByNameDesc().stream()
                .filter(existing -> "Demo Section".equalsIgnoreCase(existing.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Section created = new Section();
                    created.setId(sequenceGeneratorService.generateSequence(Section.SEQUENCE_NAME));
                    created.setName("Demo Section");
                    created.setStartDate(LocalDate.of(2026, 1, 12));
                    created.setEndDate(LocalDate.of(2026, 5, 8));
                    created.setRubricId(rubric.getId());
                    created.setStudentIds(new HashSet<>());
                    created.setInstructorIds(new HashSet<>());
                    return sectionRepo.save(created);
                });

        section.setRubricId(rubric.getId());
        section.getInstructorIds().add(instructor.getId());
        section.getStudentIds().add(sam.getId());
        section.getStudentIds().add(taylor.getId());
        sectionRepo.save(section);

        Team team = teamRepo.findAll().stream()
                .filter(existing -> "Demo Team".equalsIgnoreCase(existing.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Team created = new Team();
                    created.setId(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME));
                    created.setName("Demo Team");
                    created.setDescription("Ralph: Demo team for WAR and peer evaluation flows");
                    created.setWebsiteUrl("https://example.com/demo-team");
                    created.setStudentIds(new HashSet<>());
                    created.setInstructorIds(new HashSet<>());
                    return created;
                });

        team.setSectionId(section.getId());
        team.setInstructorIds(new HashSet<>(Set.of(instructor.getId())));
        team.setStudentIds(new HashSet<>(Set.of(sam.getId(), taylor.getId())));
        teamRepo.save(team);
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
