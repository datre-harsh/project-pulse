package edu.tcu.projectpulse.service;

import edu.tcu.projectpulse.api.ApiException;
import edu.tcu.projectpulse.domain.*;
import edu.tcu.projectpulse.dto.*;
import edu.tcu.projectpulse.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectPulseService {

    private final UserAccountRepository userRepo;
    private final SectionRepository sectionRepo;
    private final TeamRepository teamRepo;
    private final RubricCriterionRepository rubricRepo;
    private final WarActivityRepository warRepo;
    private final PeerEvaluationRepository evalRepo;
    private final PeerEvaluationCriterionScoreRepository evalScoreRepo;

    public List<UserAccount> getUsers(Role role) {
        if (role == null) {
            return userRepo.findAll();
        }
        return userRepo.findByRole(role);
    }

    public UserAccount getUser(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new ApiException("User not found"));
    }

    public UserAccount createUser(UserAccountRequest req) {
        UserAccount user = new UserAccount();
        user.setEmail(req.email());
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setRole(req.role());
        user.setActive(req.active() == null || req.active());
        return userRepo.save(user);
    }

    public UserAccount updateUser(Long id, UserAccountRequest req) {
        UserAccount user = getUser(id);
        user.setEmail(req.email());
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setRole(req.role());
        user.setActive(req.active() == null || req.active());
        return userRepo.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepo.existsById(id)) {
            throw new ApiException("User not found");
        }
        userRepo.deleteById(id);
    }

    public List<Section> getSections() {
        return sectionRepo.findAll();
    }

    public Section getSection(Long id) {
        return sectionRepo.findById(id).orElseThrow(() -> new ApiException("Section not found"));
    }

    public Section createSection(SectionRequest req) {
        validateSectionWeeks(req.activeWeekStart(), req.activeWeekEnd());
        Section section = new Section();
        section.setName(req.name());
        section.setSemester(req.semester());
        section.setYear(req.year());
        section.setActiveWeekStart(req.activeWeekStart());
        section.setActiveWeekEnd(req.activeWeekEnd());
        return sectionRepo.save(section);
    }

    public Section updateSection(Long id, SectionRequest req) {
        validateSectionWeeks(req.activeWeekStart(), req.activeWeekEnd());
        Section section = getSection(id);
        section.setName(req.name());
        section.setSemester(req.semester());
        section.setYear(req.year());
        section.setActiveWeekStart(req.activeWeekStart());
        section.setActiveWeekEnd(req.activeWeekEnd());
        return sectionRepo.save(section);
    }

    public List<Team> getTeams(Long sectionId) {
        if (sectionId == null) {
            return teamRepo.findAll();
        }
        return teamRepo.findBySectionId(sectionId);
    }

    public Team getTeam(Long id) {
        return teamRepo.findById(id).orElseThrow(() -> new ApiException("Team not found"));
    }

    public Team createTeam(TeamRequest req) {
        validateSectionExists(req.sectionId());
        Team team = new Team();
        team.setSectionId(req.sectionId());
        team.setName(req.name());
        team.setStudentIds(req.studentIds() == null ? new HashSet<>() : new HashSet<>(req.studentIds()));
        team.setInstructorIds(req.instructorIds() == null ? new HashSet<>() : new HashSet<>(req.instructorIds()));
        validateBr1(team);
        return teamRepo.save(team);
    }

    public Team updateTeam(Long id, TeamRequest req) {
        validateSectionExists(req.sectionId());
        Team team = getTeam(id);
        team.setSectionId(req.sectionId());
        team.setName(req.name());
        team.setStudentIds(req.studentIds() == null ? new HashSet<>() : new HashSet<>(req.studentIds()));
        team.setInstructorIds(req.instructorIds() == null ? new HashSet<>() : new HashSet<>(req.instructorIds()));
        validateBr1(team);
        return teamRepo.save(team);
    }

    public void deleteTeam(Long id) {
        Team team = getTeam(id);
        if (!team.getInstructorIds().isEmpty()) {
            throw new ApiException("Cannot delete team with instructors assigned. Remove instructors first.");
        }
        teamRepo.delete(team);
    }

    public Team addStudentToTeam(Long teamId, Long studentId) {
        Team team = getTeam(teamId);
        UserAccount student = getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("User is not a student");
        }
        team.getStudentIds().add(studentId);
        return teamRepo.save(team);
    }

    public Team removeStudentFromTeam(Long teamId, Long studentId) {
        Team team = getTeam(teamId);
        team.getStudentIds().remove(studentId);
        return teamRepo.save(team);
    }

    public Team addInstructorToTeam(Long teamId, Long instructorId) {
        Team team = getTeam(teamId);
        UserAccount instructor = getUser(instructorId);
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new ApiException("User is not an instructor");
        }
        team.getInstructorIds().add(instructorId);
        validateBr1(team);
        return teamRepo.save(team);
    }

    public Team removeInstructorFromTeam(Long teamId, Long instructorId) {
        Team team = getTeam(teamId);
        team.getInstructorIds().remove(instructorId);
        validateBr1(team);
        return teamRepo.save(team);
    }

    public List<RubricCriterion> getRubricCriteria() {
        return rubricRepo.findAll();
    }

    public RubricCriterion createRubricCriterion(RubricCriterionRequest req) {
        RubricCriterion criterion = new RubricCriterion();
        criterion.setName(req.name());
        criterion.setMaxScore(req.maxScore());
        criterion.setActive(req.active() == null || req.active());
        return rubricRepo.save(criterion);
    }

    public RubricCriterion updateRubricCriterion(Long id, RubricCriterionRequest req) {
        RubricCriterion criterion = rubricRepo.findById(id).orElseThrow(() -> new ApiException("Rubric criterion not found"));
        criterion.setName(req.name());
        criterion.setMaxScore(req.maxScore());
        criterion.setActive(req.active() == null || req.active());
        return rubricRepo.save(criterion);
    }

    public List<WarActivity> getWarActivities() {
        return warRepo.findAll();
    }

    public WarActivity createWarActivity(WarActivityRequest req) {
        validateSectionExists(req.sectionId());
        validateTeamMembership(req.teamId(), req.studentId(), Role.STUDENT);
        WarActivity activity = new WarActivity();
        activity.setSectionId(req.sectionId());
        activity.setTeamId(req.teamId());
        activity.setStudentId(req.studentId());
        activity.setWeekNumber(req.weekNumber());
        activity.setCategory(req.category());
        activity.setPlannedActivity(req.plannedActivity());
        activity.setDescription(req.description());
        activity.setPlannedHours(req.plannedHours());
        activity.setActualHours(req.actualHours());
        activity.setStatus(req.status());
        return warRepo.save(activity);
    }

    public WarActivity updateWarActivity(Long id, WarActivityRequest req) {
        WarActivity activity = warRepo.findById(id).orElseThrow(() -> new ApiException("WAR activity not found"));
        validateSectionExists(req.sectionId());
        validateTeamMembership(req.teamId(), req.studentId(), Role.STUDENT);
        activity.setSectionId(req.sectionId());
        activity.setTeamId(req.teamId());
        activity.setStudentId(req.studentId());
        activity.setWeekNumber(req.weekNumber());
        activity.setCategory(req.category());
        activity.setPlannedActivity(req.plannedActivity());
        activity.setDescription(req.description());
        activity.setPlannedHours(req.plannedHours());
        activity.setActualHours(req.actualHours());
        activity.setStatus(req.status());
        return warRepo.save(activity);
    }

    public void deleteWarActivity(Long id) {
        warRepo.deleteById(id);
    }

    @Transactional
    public PeerEvaluation submitPeerEvaluation(PeerEvaluationRequest req) {
        validateSectionExists(req.sectionId());
        validateTeamMembership(req.teamId(), req.evaluatorStudentId(), Role.STUDENT);
        validateTeamMembership(req.teamId(), req.evaluateeStudentId(), Role.STUDENT);

        if (Objects.equals(req.evaluatorStudentId(), req.evaluateeStudentId())) {
            throw new ApiException("Students cannot evaluate themselves");
        }

        Section section = getSection(req.sectionId());

        if (req.targetWeekNumber() < section.getActiveWeekStart() || req.targetWeekNumber() > section.getActiveWeekEnd()) {
            throw new ApiException("BR-2 violation: Peer evaluation can only be submitted for active weeks");
        }

        int currentWeek = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
        if (req.targetWeekNumber() != currentWeek - 1) {
            throw new ApiException("BR-4 violation: Peer evaluation must target the previous week only");
        }

        if (evalRepo.findByEvaluatorStudentIdAndEvaluateeStudentIdAndTargetWeekNumber(
                req.evaluatorStudentId(), req.evaluateeStudentId(), req.targetWeekNumber()).isPresent()) {
            throw new ApiException("BR-3 violation: Peer evaluation cannot be edited once completed");
        }

        int total = 0;
        for (CriterionScoreRequest scoreRequest : req.scores()) {
            RubricCriterion criterion = rubricRepo.findById(scoreRequest.criterionId())
                    .orElseThrow(() -> new ApiException("Rubric criterion not found: " + scoreRequest.criterionId()));
            if (!criterion.isActive()) {
                throw new ApiException("Inactive rubric criterion cannot be used");
            }
            if (scoreRequest.score() < 0 || scoreRequest.score() > criterion.getMaxScore()) {
                throw new ApiException("Criterion score out of allowed range for criterion: " + criterion.getName());
            }
            total += scoreRequest.score();
        }

        PeerEvaluation eval = new PeerEvaluation();
        eval.setSectionId(req.sectionId());
        eval.setTeamId(req.teamId());
        eval.setEvaluatorStudentId(req.evaluatorStudentId());
        eval.setEvaluateeStudentId(req.evaluateeStudentId());
        eval.setTargetWeekNumber(req.targetWeekNumber());
        eval.setTotalScore(total);
        eval.setPublicComment(req.publicComment());
        eval.setSubmittedDate(LocalDate.now());
        PeerEvaluation saved = evalRepo.save(eval);

        List<PeerEvaluationCriterionScore> scoreRows = new ArrayList<>();
        for (CriterionScoreRequest scoreRequest : req.scores()) {
            PeerEvaluationCriterionScore row = new PeerEvaluationCriterionScore();
            row.setEvaluationId(saved.getId());
            row.setCriterionId(scoreRequest.criterionId());
            row.setScore(scoreRequest.score());
            scoreRows.add(row);
        }
        evalScoreRepo.saveAll(scoreRows);

        return saved;
    }

    public List<Map<String, Object>> getStudentPeerEvaluationReport(Long studentId) {
        List<PeerEvaluation> evaluations = evalRepo.findByEvaluateeStudentIdAndTargetWeekNumberBetween(studentId, 1, 20);
        List<Map<String, Object>> report = new ArrayList<>();

        for (PeerEvaluation eval : evaluations) {
            Map<String, Object> row = new HashMap<>();
            row.put("evaluationId", eval.getId());
            row.put("targetWeek", eval.getTargetWeekNumber());
            row.put("totalScore", eval.getTotalScore());
            row.put("publicComment", eval.getPublicComment());
            row.put("criterionScores", evalScoreRepo.findByEvaluationId(eval.getId()));
            report.add(row);
        }

        return report;
    }

    public List<PeerEvaluation> generatePeerEvaluationReport(PeerEvaluationReportRequest req) {
        if (req.startWeek() > req.endWeek()) {
            throw new ApiException("Start week cannot be greater than end week");
        }
        if (req.bySection()) {
            return evalRepo.findBySectionIdAndTargetWeekNumberBetween(req.targetId(), req.startWeek(), req.endWeek());
        }
        return evalRepo.findByEvaluateeStudentIdAndTargetWeekNumberBetween(req.targetId(), req.startWeek(), req.endWeek());
    }

    public List<WarActivity> generateWarReport(WarReportRequest req) {
        if (req.startWeek() > req.endWeek()) {
            throw new ApiException("Start week cannot be greater than end week");
        }
        if (req.byTeam()) {
            return warRepo.findByTeamIdAndWeekNumberBetweenOrderByWeekNumberAsc(req.targetId(), req.startWeek(), req.endWeek());
        }
        return warRepo.findByStudentIdAndWeekNumberBetweenOrderByWeekNumberAsc(req.targetId(), req.startWeek(), req.endWeek());
    }

    private void validateSectionWeeks(Integer start, Integer end) {
        if (start > end) {
            throw new ApiException("Active week start must be <= active week end");
        }
    }

    private void validateSectionExists(Long sectionId) {
        if (!sectionRepo.existsById(sectionId)) {
            throw new ApiException("Section not found");
        }
    }

    private void validateTeamMembership(Long teamId, Long userId, Role role) {
        Team team = getTeam(teamId);
        UserAccount user = getUser(userId);
        if (user.getRole() != role) {
            throw new ApiException("Role mismatch for user");
        }
        Set<Long> ids = role == Role.STUDENT ? team.getStudentIds() : team.getInstructorIds();
        if (!ids.contains(userId)) {
            throw new ApiException("User is not assigned to team");
        }
    }

    private void validateBr1(Team team) {
        if (team.getInstructorIds() == null || team.getInstructorIds().isEmpty()) {
            throw new ApiException("BR-1 violation: Every senior design team must have at least one instructor");
        }

        Set<Long> validated = team.getInstructorIds().stream().map(this::getUser).filter(u -> u.getRole() == Role.INSTRUCTOR).map(UserAccount::getId).collect(Collectors.toSet());
        if (validated.size() != team.getInstructorIds().size()) {
            throw new ApiException("All instructor IDs must belong to INSTRUCTOR role users");
        }
    }
}
