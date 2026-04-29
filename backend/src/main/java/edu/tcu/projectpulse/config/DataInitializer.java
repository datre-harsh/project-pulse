package edu.tcu.projectpulse.config;

import edu.tcu.projectpulse.domain.Role;
import edu.tcu.projectpulse.domain.Rubric;
import edu.tcu.projectpulse.domain.RubricCriterion;
import edu.tcu.projectpulse.domain.Section;
import edu.tcu.projectpulse.domain.Team;
import edu.tcu.projectpulse.domain.UserAccount;
import edu.tcu.projectpulse.domain.ActivityCategory;
import edu.tcu.projectpulse.domain.ActivityStatus;
import edu.tcu.projectpulse.domain.PeerEvaluation;
import edu.tcu.projectpulse.domain.WeeklyActivity;
import edu.tcu.projectpulse.repo.PeerEvaluationRepository;
import edu.tcu.projectpulse.repo.RubricCriterionRepository;
import edu.tcu.projectpulse.repo.RubricRepository;
import edu.tcu.projectpulse.repo.SectionRepository;
import edu.tcu.projectpulse.repo.TeamRepository;
import edu.tcu.projectpulse.repo.UserAccountRepository;
import edu.tcu.projectpulse.repo.WeeklyActivityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userRepo;
    private final RubricRepository rubricRepo;
    private final RubricCriterionRepository rubricCriterionRepo;
    private final SectionRepository sectionRepo;
    private final TeamRepository teamRepo;
    private final WeeklyActivityRepository weeklyActivityRepo;
    private final PeerEvaluationRepository peerEvaluationRepo;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(
            UserAccountRepository userRepo,
            RubricRepository rubricRepo,
            RubricCriterionRepository rubricCriterionRepo,
            SectionRepository sectionRepo,
            TeamRepository teamRepo,
            WeeklyActivityRepository weeklyActivityRepo,
            PeerEvaluationRepository peerEvaluationRepo,
            SequenceGeneratorService sequenceGeneratorService
    ) {
        this.userRepo = userRepo;
        this.rubricRepo = rubricRepo;
        this.rubricCriterionRepo = rubricCriterionRepo;
        this.sectionRepo = sectionRepo;
        this.teamRepo = teamRepo;
        this.weeklyActivityRepo = weeklyActivityRepo;
        this.peerEvaluationRepo = peerEvaluationRepo;
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
        createUserIfMissing("mia.chen@projectpulse.local", "Mia", "Chen", Role.STUDENT);
        createUserIfMissing("noah.garcia@projectpulse.local", "Noah", "Garcia", Role.STUDENT);
        createUserIfMissing("priya.shah@projectpulse.local", "Priya", "Shah", Role.STUDENT);
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

        Set<String> existingCriteria = rubricCriterionRepo.findByRubricIdOrderByIdAsc(rubric.getId()).stream()
                .map(criterion -> criterion.getName().toLowerCase())
                .collect(Collectors.toSet());

        createCriterionIfMissing(existingCriteria, rubric.getId(), "Quality of work", "How do you rate the quality of this teammate's work?", BigDecimal.TEN);
        createCriterionIfMissing(existingCriteria, rubric.getId(), "Productivity", "How productive is this teammate?", BigDecimal.TEN);
        createCriterionIfMissing(existingCriteria, rubric.getId(), "Initiative", "How proactive is this teammate?", BigDecimal.TEN);
        createCriterionIfMissing(existingCriteria, rubric.getId(), "Collaboration", "How well does this teammate communicate and support the team?", BigDecimal.TEN);
        createCriterionIfMissing(existingCriteria, rubric.getId(), "Reliability", "How consistently does this teammate complete promised work?", BigDecimal.TEN);
        createCriterionIfMissing(existingCriteria, rubric.getId(), "Professionalism", "How professionally does this teammate participate in meetings and reviews?", BigDecimal.TEN);
        return rubric;
    }

    private void seedDemoTeam(Rubric rubric) {
        UserAccount instructor = userRepo.findByEmailIgnoreCase("instructor@projectpulse.local").orElse(null);
        UserAccount sam = userRepo.findByEmailIgnoreCase("student1@projectpulse.local").orElse(null);
        UserAccount taylor = userRepo.findByEmailIgnoreCase("student2@projectpulse.local").orElse(null);
        UserAccount harsh = userRepo.findByEmailIgnoreCase("harsh.mehta@tcu.edu").orElse(null);
        UserAccount ralph = userRepo.findByEmailIgnoreCase("ralph.nguyen@tcu.edu").orElse(null);
        UserAccount jenny = userRepo.findByEmailIgnoreCase("jenny.patel@tcu.edu").orElse(null);
        UserAccount mia = userRepo.findByEmailIgnoreCase("mia.chen@projectpulse.local").orElse(null);
        UserAccount noah = userRepo.findByEmailIgnoreCase("noah.garcia@projectpulse.local").orElse(null);
        UserAccount priya = userRepo.findByEmailIgnoreCase("priya.shah@projectpulse.local").orElse(null);

        if (instructor == null || sam == null || taylor == null || harsh == null || ralph == null || jenny == null
                || mia == null || noah == null || priya == null) {
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
        section.getStudentIds().add(harsh.getId());
        section.getStudentIds().add(ralph.getId());
        section.getStudentIds().add(jenny.getId());
        section.getStudentIds().add(mia.getId());
        section.getStudentIds().add(noah.getId());
        section.getStudentIds().add(priya.getId());
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
        team.setStudentIds(new HashSet<>(Set.of(sam.getId(), taylor.getId(), harsh.getId(), ralph.getId())));
        teamRepo.save(team);

        Team capstoneTeam = teamRepo.findAll().stream()
                .filter(existing -> "Project Pulse Capstone Team".equalsIgnoreCase(existing.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Team created = new Team();
                    created.setId(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME));
                    created.setName("Project Pulse Capstone Team");
                    created.setDescription("Demo team with complete WAR and peer evaluation data");
                    created.setWebsiteUrl("https://project-pulse-42048.azurewebsites.net");
                    created.setStudentIds(new HashSet<>());
                    created.setInstructorIds(new HashSet<>());
                    return created;
                });
        capstoneTeam.setSectionId(section.getId());
        capstoneTeam.setInstructorIds(new HashSet<>(Set.of(instructor.getId())));
        capstoneTeam.setStudentIds(new HashSet<>(Set.of(jenny.getId(), mia.getId(), noah.getId(), priya.getId())));
        teamRepo.save(capstoneTeam);

        seedWeeklyActivities(sam, taylor, harsh, ralph, jenny, mia, noah, priya);
        seedPeerEvaluations(rubric, sam, taylor, harsh, ralph, jenny, mia, noah, priya);
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

    private void createCriterionIfMissing(Set<String> existingCriteria, Long rubricId, String name, String description, BigDecimal maxScore) {
        if (existingCriteria.contains(name.toLowerCase())) {
            return;
        }
        createCriterion(rubricId, name, description, maxScore);
        existingCriteria.add(name.toLowerCase());
    }

    private void seedWeeklyActivities(UserAccount sam, UserAccount taylor, UserAccount harsh, UserAccount ralph,
                                      UserAccount jenny, UserAccount mia, UserAccount noah, UserAccount priya) {
        seedActivity(sam, "2024-week5", ActivityCategory.DEVELOPMENT,
                "Implemented login validation and role-based navigation for the student dashboard.",
                6.0, 7.0, ActivityStatus.DONE, 1);
        seedActivity(sam, "2024-week5", ActivityCategory.TESTING,
                "Tested student registration, profile updates, and section visibility.",
                3.0, 2.5, ActivityStatus.DONE, 2);
        seedActivity(taylor, "2024-week5", ActivityCategory.DOCUMENTATION,
                "Prepared demo script notes for admin and instructor use cases.",
                4.0, 4.0, ActivityStatus.DONE, 1);
        seedActivity(harsh, "2024-week5", ActivityCategory.DEPLOYMENT,
                "Configured Azure deployment and verified production login with seeded accounts.",
                5.0, 6.0, ActivityStatus.DONE, 1);
        seedActivity(ralph, "2024-week5", ActivityCategory.BUGFIX,
                "Fixed peer evaluation report filters and checked WAR report output.",
                5.0, 5.5, ActivityStatus.UNDER_TESTING, 1);

        seedActivity(jenny, "2024-week5", ActivityCategory.DESIGN,
                "Refined instructor report layout and reviewed rubric criteria display.",
                4.0, 4.5, ActivityStatus.DONE, 1);
        seedActivity(mia, "2024-week5", ActivityCategory.DEVELOPMENT,
                "Connected WAR form changes to the backend activity API.",
                6.0, 6.5, ActivityStatus.DONE, 1);
        seedActivity(noah, "2024-week5", ActivityCategory.TESTING,
                "Ran demo-path testing for peer evaluations from multiple student accounts.",
                4.0, 4.0, ActivityStatus.DONE, 1);

        seedActivity(sam, "2024-week4", ActivityCategory.PLANNING,
                "Broke down remaining use cases and assigned demo responsibilities.",
                2.0, 2.0, ActivityStatus.DONE, 1);
        seedActivity(harsh, "2024-week4", ActivityCategory.DEVELOPMENT,
                "Added real authentication flow for seeded users.",
                7.0, 8.0, ActivityStatus.DONE, 1);
        seedActivity(mia, "2024-week4", ActivityCategory.COMMUNICATION,
                "Collected teammate feedback and updated meeting notes.",
                3.0, 3.0, ActivityStatus.DONE, 1);

        // Priya intentionally has no 2024-week5 WAR so the team WAR report can show a missing student.
        seedActivity(priya, "2024-week4", ActivityCategory.LEARNING,
                "Reviewed Project Pulse requirements and mapped expected instructor reports.",
                3.0, 2.0, ActivityStatus.DONE, 1);
    }

    private void seedActivity(UserAccount student, String weekId, ActivityCategory category, String description,
                              double plannedHours, double actualHours, ActivityStatus status, int rowNumber) {
        String id = "demo-war-" + student.getId() + "-" + weekId + "-" + rowNumber;
        if (weeklyActivityRepo.findById(id).isPresent()) {
            return;
        }

        WeeklyActivity activity = new WeeklyActivity();
        activity.setId(id);
        activity.setStudentId(student.getId().toString());
        activity.setWeekId(weekId);
        activity.setCategory(category);
        activity.setDescription(description);
        activity.setPlannedHours(plannedHours);
        activity.setActualHours(actualHours);
        activity.setStatus(status);
        activity.setCreatedAt(LocalDateTime.now().minusDays(7L - rowNumber));
        activity.setUpdatedAt(LocalDateTime.now().minusDays(6L - rowNumber));
        weeklyActivityRepo.save(activity);
    }

    private void seedPeerEvaluations(Rubric rubric, UserAccount sam, UserAccount taylor, UserAccount harsh, UserAccount ralph,
                                     UserAccount jenny, UserAccount mia, UserAccount noah, UserAccount priya) {
        seedPeerEvaluation(sam, taylor, "2024-week5", 9,
                "Taylor kept the demo notes organized and easy to follow.",
                "Taylor is reliable and ready for the recorded walkthrough.");
        seedPeerEvaluation(taylor, sam, "2024-week5", 8,
                "Sam finished the student-facing pieces and communicated blockers early.",
                "Sam could add a little more test evidence before final submission.");
        seedPeerEvaluation(harsh, ralph, "2024-week5", 9,
                "Ralph helped validate the report screens and caught issues quickly.",
                "Ralph is strong on quality checks and debugging.");
        seedPeerEvaluation(ralph, harsh, "2024-week5", 10,
                "Harsh handled setup, deployment, and production verification.",
                "Harsh should lead the deployment part of the demo.");

        seedPeerEvaluation(mia, jenny, "2024-week5", 9,
                "Jenny improved the report readability and helped with rubric review.",
                "Jenny is a good candidate to explain instructor report output.");
        seedPeerEvaluation(noah, jenny, "2024-week5", 8,
                "Jenny was responsive during UI review and made useful suggestions.",
                "Jenny should keep sharing screenshots before merging changes.");
        seedPeerEvaluation(jenny, mia, "2024-week5", 9,
                "Mia connected the WAR flow and kept the API behavior consistent.",
                "Mia's work is demo-ready.");
        seedPeerEvaluation(priya, mia, "2024-week5", 8,
                "Mia helped explain backend expectations and supported testing.",
                "Mia may need to document the API endpoints more clearly.");
        seedPeerEvaluation(jenny, noah, "2024-week5", 8,
                "Noah tested the demo paths from several roles.",
                "Noah should mention the exact accounts used during the recording.");
        seedPeerEvaluation(mia, noah, "2024-week5", 9,
                "Noah found login and report issues before the demo.",
                "Noah's testing notes are useful for the final report.");

        seedPeerEvaluation(sam, taylor, "2024-week4", 8,
                "Taylor helped plan the use case demo order.",
                "Good planning contribution.");
        seedPeerEvaluation(taylor, harsh, "2024-week4", 9,
                "Harsh completed the authentication setup work.",
                "Strong ownership on setup tasks.");
    }

    private void seedPeerEvaluation(UserAccount evaluator, UserAccount evaluatee, String weekId, int baseScore,
                                    String publicComment, String privateComment) {
        if (peerEvaluationRepo.findByEvaluatorIdAndEvaluateeIdAndWeekId(evaluator.getId(), evaluatee.getId(), weekId).isPresent()) {
            return;
        }

        PeerEvaluation evaluation = new PeerEvaluation();
        evaluation.setId(sequenceGeneratorService.generateSequence(PeerEvaluation.SEQUENCE_NAME));
        evaluation.setEvaluatorId(evaluator.getId());
        evaluation.setEvaluateeId(evaluatee.getId());
        evaluation.setWeekId(weekId);
        evaluation.setScores(scoreMap(baseScore));
        evaluation.setPublicComment(publicComment);
        evaluation.setPrivateComment(privateComment);
        evaluation.setCreatedAt(LocalDateTime.now().minusDays(2));
        evaluation.setUpdatedAt(LocalDateTime.now().minusDays(2));
        peerEvaluationRepo.save(evaluation);
    }

    private Map<String, Integer> scoreMap(int baseScore) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Quality of work", baseScore);
        scores.put("Productivity", Math.max(1, baseScore - 1));
        scores.put("Initiative", baseScore);
        scores.put("Collaboration", Math.min(10, baseScore + 1));
        scores.put("Reliability", baseScore);
        scores.put("Professionalism", Math.max(1, baseScore - 1));
        return scores;
    }
}
