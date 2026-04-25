package edu.tcu.projectpulse.service;

import edu.tcu.projectpulse.api.ApiException;
import edu.tcu.projectpulse.config.SequenceGeneratorService;
import edu.tcu.projectpulse.domain.Role;
import edu.tcu.projectpulse.domain.Rubric;
import edu.tcu.projectpulse.domain.RubricCriterion;
import edu.tcu.projectpulse.domain.Section;
import edu.tcu.projectpulse.domain.StudentInvitation;
import edu.tcu.projectpulse.domain.Team;
import edu.tcu.projectpulse.domain.UserAccount;
import edu.tcu.projectpulse.dto.*;
import edu.tcu.projectpulse.repo.RubricCriterionRepository;
import edu.tcu.projectpulse.repo.RubricRepository;
import edu.tcu.projectpulse.repo.SectionRepository;
import edu.tcu.projectpulse.repo.StudentInvitationRepository;
import edu.tcu.projectpulse.repo.TeamRepository;
import edu.tcu.projectpulse.repo.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectPulseService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserAccountRepository userRepo;
    private final SectionRepository sectionRepo;
    private final TeamRepository teamRepo;
    private final RubricRepository rubricRepo;
    private final RubricCriterionRepository rubricRepoCriteria;
    private final StudentInvitationRepository invitationRepo;
    private final SequenceGeneratorService sequenceGeneratorService;

    public List<RubricDetailResponse> getRubrics(String name) {
        List<Rubric> rubrics = (name == null || name.isBlank())
                ? rubricRepo.findAll().stream().sorted(Comparator.comparing(Rubric::getName, String.CASE_INSENSITIVE_ORDER)).toList()
                : rubricRepo.findByNameContainingIgnoreCaseOrderByNameAsc(name.trim());
        return rubrics.stream().map(this::toRubricDetail).toList();
    }

    public RubricDetailResponse getRubric(Long id) {
        return toRubricDetail(getRubricEntity(id));
    }

    public RubricDetailResponse createRubric(RubricRequest req) {
        validateRubricName(req.name(), null);
        List<RubricCriterionRequest> criteria = validateRubricCriteria(req.criteria());

        Rubric rubric = new Rubric();
        rubric.setId(sequenceGeneratorService.generateSequence(Rubric.SEQUENCE_NAME));
        rubric.setName(req.name().trim());
        Rubric saved = rubricRepo.save(rubric);
        saveRubricCriteria(saved.getId(), criteria);
        return toRubricDetail(saved);
    }

    public RubricDetailResponse updateRubric(Long id, RubricRequest req) {
        Rubric rubric = getRubricEntity(id);
        validateRubricName(req.name(), id);
        List<RubricCriterionRequest> criteria = validateRubricCriteria(req.criteria());

        rubric.setName(req.name().trim());
        Rubric saved = rubricRepo.save(rubric);

        List<RubricCriterion> existing = rubricRepoCriteria.findByRubricIdOrderByIdAsc(id);
        rubricRepoCriteria.deleteAll(existing);
        saveRubricCriteria(saved.getId(), criteria);
        return toRubricDetail(saved);
    }

    public UserAccount getUser(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new ApiException("User not found"));
    }

    public List<UserSummaryResponse> getInstructorOptions() {
        return userRepo.findByRoleOrderByLastNameAscFirstNameAsc(Role.INSTRUCTOR).stream()
                .filter(UserAccount::isActive)
                .map(this::toUserSummary)
                .toList();
    }

    public List<SectionSummaryResponse> getSections(String name) {
        List<Section> sections = (name == null || name.isBlank())
                ? sectionRepo.findAllByOrderByNameDesc()
                : sectionRepo.findByNameContainingIgnoreCaseOrderByNameDesc(name.trim());
        return sections.stream().map(this::toSectionSummary).toList();
    }

    public SectionDetailResponse getSection(Long id) {
        return toSectionDetail(getSectionEntity(id));
    }

    public SectionDetailResponse createSection(SectionRequest req) {
        validateSectionRequest(req, null);

        Section section = new Section();
        section.setId(sequenceGeneratorService.generateSequence(Section.SEQUENCE_NAME));
        applySection(section, req);
        Section saved = sectionRepo.save(section);
        return toSectionDetail(saved);
    }

    public SectionDetailResponse updateSection(Long id, SectionRequest req) {
        validateSectionRequest(req, id);
        Section section = getSectionEntity(id);
        applySection(section, req);
        Section saved = sectionRepo.save(section);
        return toSectionDetail(saved);
    }

    public SectionDetailResponse updateActiveWeeks(Long id, ActiveWeeksRequest req) {
        Section section = getSectionEntity(id);
        Set<Integer> inactive = sanitizeWeeks(req == null ? null : req.inactiveWeekNumbers());
        validateInactiveWeeks(section, inactive);
        section.setInactiveWeekNumbers(inactive);
        return toSectionDetail(sectionRepo.save(section));
    }

    public List<StudentInvitationResponse> inviteStudents(Long id, StudentInvitationRequest req) {
        if (!Objects.equals(id, req.sectionId())) {
            throw new ApiException("Section ID mismatch");
        }
        Section section = getSectionEntity(id);
        List<String> emails = parseEmails(req.emails());

        String subject = req.subject() == null || req.subject().isBlank()
                ? "Welcome to Project Pulse - Complete Your Registration"
                : req.subject().trim();

        List<StudentInvitationResponse> responses = new ArrayList<>();
        for (String email : emails) {
            String token = UUID.randomUUID().toString().replace("-", "");
            String message = buildInvitationMessage(email, token, req.message());

            StudentInvitation invitation = new StudentInvitation();
            invitation.setId(sequenceGeneratorService.generateSequence(StudentInvitation.SEQUENCE_NAME));
            invitation.setSectionId(section.getId());
            invitation.setEmail(email);
            invitation.setToken(token);
            invitation.setSubject(subject);
            invitation.setMessage(message);
            invitation.setSentAt(LocalDateTime.now());
            StudentInvitation saved = invitationRepo.save(invitation);
            responses.add(toStudentInvitation(saved));
        }
        return responses;
    }

    public List<TeamSummaryResponse> getTeams(Long sectionId, String sectionName, String teamName, Long instructorId) {
        List<Team> teams = sectionId == null ? teamRepo.findAll() : teamRepo.findBySectionId(sectionId);
        return teams.stream()
                .filter(team -> matchesSectionName(team, sectionName))
                .filter(team -> matchesTeamName(team, teamName))
                .filter(team -> instructorId == null || team.getInstructorIds().contains(instructorId))
                .map(this::toTeamSummary)
                .sorted(Comparator.comparing(TeamSummaryResponse::sectionName, String.CASE_INSENSITIVE_ORDER).reversed()
                        .thenComparing(TeamSummaryResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public TeamDetailResponse getTeam(Long id) {
        return toTeamDetail(getTeamEntity(id));
    }

    public TeamDetailResponse createTeam(TeamRequest req) {
        validateTeamRequest(req, null);
        Team team = new Team();
        team.setId(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME));
        applyTeam(team, req);
        Team saved = teamRepo.save(team);
        syncSectionMemberships(saved);
        return toTeamDetail(saved);
    }

    public TeamDetailResponse updateTeam(Long id, TeamRequest req) {
        validateTeamRequest(req, id);
        Team team = getTeamEntity(id);
        applyTeam(team, req);
        Team saved = teamRepo.save(team);
        syncSectionMemberships(saved);
        return toTeamDetail(saved);
    }

    public void deleteTeam(Long id) {
        Team team = getTeamEntity(id);
        if (!team.getInstructorIds().isEmpty()) {
            throw new ApiException("Cannot delete team with instructors assigned. Remove instructors first.");
        }
        teamRepo.delete(team);
    }

    public List<RubricCriterionResponse> getRubricCriteria(Long sectionId) {
        Section section = getSectionEntity(sectionId);
        return rubricRepoCriteria.findByRubricIdOrderByIdAsc(section.getRubricId()).stream()
                .map(this::toRubricCriterionResponse)
                .toList();
    }

    public List<RubricCriterionResponse> getRubricCriteria() {
        return rubricRepo.findAll().stream()
                .flatMap(rubric -> rubricRepoCriteria.findByRubricIdOrderByIdAsc(rubric.getId()).stream())
                .map(this::toRubricCriterionResponse)
                .toList();
    }

    public RubricDetailResponse getSectionRubric(Long sectionId) {
        Section section = getSectionEntity(sectionId);
        return toRubricDetail(getRubricEntity(section.getRubricId()));
    }

    private void saveRubricCriteria(Long rubricId, List<RubricCriterionRequest> criteria) {
        List<RubricCriterion> rows = criteria.stream().map(req -> {
            RubricCriterion criterion = new RubricCriterion();
            criterion.setId(sequenceGeneratorService.generateSequence(RubricCriterion.SEQUENCE_NAME));
            criterion.setRubricId(rubricId);
            criterion.setName(req.name().trim());
            criterion.setDescription(req.description().trim());
            criterion.setMaxScore(req.maxScore());
            criterion.setActive(req.active() == null || req.active());
            return criterion;
        }).toList();
        rubricRepoCriteria.saveAll(rows);
    }

    private List<RubricCriterionRequest> validateRubricCriteria(List<RubricCriterionRequest> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            throw new ApiException("A rubric must include at least one criterion");
        }
        for (RubricCriterionRequest criterion : criteria) {
            if (criterion.maxScore().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("Criterion max score must be positive");
            }
        }
        return criteria;
    }

    private void validateRubricName(String name, Long existingId) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new ApiException("Rubric name is required");
        }
        List<Rubric> matches = rubricRepo.findByNameContainingIgnoreCaseOrderByNameAsc(normalized).stream()
                .filter(r -> r.getName().equalsIgnoreCase(normalized))
                .toList();
        boolean duplicate = matches.stream().anyMatch(r -> existingId == null || !r.getId().equals(existingId));
        if (duplicate) {
            throw new ApiException("Rubric name must be unique");
        }
    }

    private void validateSectionRequest(SectionRequest req, Long existingId) {
        validateSectionDates(req.startDate(), req.endDate());
        validateSectionName(req.name(), existingId);
        getRubricEntity(req.rubricId());
        validateUsersByRole(req.studentIds(), Role.STUDENT);
        validateUsersByRole(req.instructorIds(), Role.INSTRUCTOR);
        if (req.inactiveWeekNumbers() != null) {
            validateInactiveWeeks(req.startDate(), req.endDate(), sanitizeWeeks(req.inactiveWeekNumbers()));
        }
    }

    private void applySection(Section section, SectionRequest req) {
        section.setName(req.name().trim());
        section.setStartDate(req.startDate());
        section.setEndDate(req.endDate());
        section.setRubricId(req.rubricId());
        if (req.studentIds() != null) {
            section.setStudentIds(new HashSet<>(req.studentIds()));
        } else if (section.getStudentIds() == null) {
            section.setStudentIds(new HashSet<>());
        }
        if (req.instructorIds() != null) {
            section.setInstructorIds(new HashSet<>(req.instructorIds()));
        } else if (section.getInstructorIds() == null) {
            section.setInstructorIds(new HashSet<>());
        }
        if (req.inactiveWeekNumbers() != null) {
            section.setInactiveWeekNumbers(sanitizeWeeks(req.inactiveWeekNumbers()));
        } else if (section.getInactiveWeekNumbers() == null) {
            section.setInactiveWeekNumbers(new HashSet<>());
        }
    }

    private void validateSectionName(String name, Long existingId) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new ApiException("Section name is required");
        }
        List<Section> matches = sectionRepo.findByNameContainingIgnoreCaseOrderByNameDesc(normalized).stream()
                .filter(section -> section.getName().equalsIgnoreCase(normalized))
                .toList();
        boolean duplicate = matches.stream().anyMatch(section -> existingId == null || !section.getId().equals(existingId));
        if (duplicate) {
            throw new ApiException("Section name must be unique");
        }
    }

    private void validateSectionDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ApiException("Section start date must be on or before the end date");
        }
    }

    private void validateInactiveWeeks(Section section, Set<Integer> inactiveWeeks) {
        validateInactiveWeeks(section.getStartDate(), section.getEndDate(), inactiveWeeks);
    }

    private void validateInactiveWeeks(LocalDate startDate, LocalDate endDate, Set<Integer> inactiveWeeks) {
        Set<Integer> availableWeeks = getWeekNumbers(startDate, endDate);
        if (!availableWeeks.containsAll(inactiveWeeks)) {
            throw new ApiException("Inactive weeks must fall within the section date range");
        }
    }

    private Set<Integer> sanitizeWeeks(Set<Integer> weeks) {
        if (weeks == null) {
            return new HashSet<>();
        }
        return weeks.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private void validateTeamRequest(TeamRequest req, Long existingId) {
        Section section = getSectionEntity(req.sectionId());
        validateTeamName(req.name(), existingId);
        validateUsersByRole(req.instructorIds(), Role.INSTRUCTOR);
        validateUsersByRole(req.studentIds(), Role.STUDENT);

        Set<Long> sectionStudents = section.getStudentIds() == null ? Set.of() : section.getStudentIds();
        for (Long studentId : req.studentIds() == null ? Set.<Long>of() : req.studentIds()) {
            if (!sectionStudents.contains(studentId)) {
                throw new ApiException("Student " + studentId + " is not part of the selected section");
            }
        }

        Team probe = new Team();
        probe.setInstructorIds(req.instructorIds() == null ? new HashSet<>() : new HashSet<>(req.instructorIds()));
        validateBr1(probe);
    }

    private void applyTeam(Team team, TeamRequest req) {
        team.setSectionId(req.sectionId());
        team.setName(req.name().trim());
        team.setDescription(req.description().trim());
        team.setWebsiteUrl(req.websiteUrl() == null || req.websiteUrl().isBlank() ? null : req.websiteUrl().trim());
        team.setStudentIds(req.studentIds() == null ? new HashSet<>() : new HashSet<>(req.studentIds()));
        team.setInstructorIds(req.instructorIds() == null ? new HashSet<>() : new HashSet<>(req.instructorIds()));
        validateBr1(team);
    }

    private void validateTeamName(String name, Long existingId) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new ApiException("Team name is required");
        }
        List<Team> matches = teamRepo.findAll().stream()
                .filter(team -> team.getName().equalsIgnoreCase(normalized))
                .toList();
        boolean duplicate = matches.stream().anyMatch(team -> existingId == null || !team.getId().equals(existingId));
        if (duplicate) {
            throw new ApiException("Team name must be unique");
        }
    }

    private void validateUsersByRole(Set<Long> ids, Role role) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            UserAccount user = getUser(id);
            if (user.getRole() != role) {
                throw new ApiException("User " + id + " must have role " + role);
            }
        }
    }

    private List<String> parseEmails(String emails) {
        List<String> parsed = Arrays.stream(emails.split(";"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        if (parsed.isEmpty()) {
            throw new ApiException("At least one student email is required");
        }
        for (String email : parsed) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new ApiException("Invalid email format: " + email);
            }
        }
        return parsed;
    }

    private String buildInvitationMessage(String email, String token, String customMessage) {
        if (customMessage != null && !customMessage.isBlank()) {
            return customMessage.replace("[Registration link]", buildRegistrationLink(token)).trim();
        }
        return ("Project Pulse has invited " + email + " to join the section.\n\n"
                + "To complete registration, use this link:\n"
                + buildRegistrationLink(token) + "\n\n"
                + "If you need help, contact your course admin.").trim();
    }

    private String buildRegistrationLink(String token) {
        return "https://project-pulse.local/student-registration?token=" + token;
    }

    private boolean matchesSectionName(Team team, String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return true;
        }
        Section section = getSectionEntity(team.getSectionId());
        return section.getName().toLowerCase(Locale.ROOT).contains(sectionName.trim().toLowerCase(Locale.ROOT));
    }

    private boolean matchesTeamName(Team team, String teamName) {
        return teamName == null
                || teamName.isBlank()
                || team.getName().toLowerCase(Locale.ROOT).contains(teamName.trim().toLowerCase(Locale.ROOT));
    }

    private void syncSectionMemberships(Team team) {
        Section section = getSectionEntity(team.getSectionId());
        section.getInstructorIds().addAll(team.getInstructorIds());
        section.getStudentIds().addAll(team.getStudentIds());
        sectionRepo.save(section);
    }

    private void validateSectionExists(Long sectionId) {
        if (!sectionRepo.existsById(sectionId)) {
            throw new ApiException("Section not found");
        }
    }

    private void validateTeamBelongsToSection(Long teamId, Long sectionId) {
        Team team = getTeamEntity(teamId);
        if (!Objects.equals(team.getSectionId(), sectionId)) {
            throw new ApiException("Team does not belong to the specified section");
        }
    }

    private void validateBr1(Team team) {
        if (team.getInstructorIds() == null || team.getInstructorIds().isEmpty()) {
            throw new ApiException("BR-1 violation: Every senior design team must have at least one instructor");
        }

        Set<Long> validated = team.getInstructorIds().stream()
                .map(this::getUser)
                .filter(user -> user.getRole() == Role.INSTRUCTOR)
                .map(UserAccount::getId)
                .collect(Collectors.toSet());
        if (validated.size() != team.getInstructorIds().size()) {
            throw new ApiException("All instructor IDs must belong to INSTRUCTOR role users");
        }
    }

    private Rubric getRubricEntity(Long id) {
        return rubricRepo.findById(id).orElseThrow(() -> new ApiException("Rubric not found"));
    }

    private Section getSectionEntity(Long id) {
        return sectionRepo.findById(id).orElseThrow(() -> new ApiException("Section not found"));
    }

    private Team getTeamEntity(Long id) {
        return teamRepo.findById(id).orElseThrow(() -> new ApiException("Team not found"));
    }

    private RubricDetailResponse toRubricDetail(Rubric rubric) {
        List<RubricCriterionResponse> criteria = rubricRepoCriteria.findByRubricIdOrderByIdAsc(rubric.getId()).stream()
                .map(this::toRubricCriterionResponse)
                .toList();
        return new RubricDetailResponse(rubric.getId(), rubric.getName(), criteria);
    }

    private RubricCriterionResponse toRubricCriterionResponse(RubricCriterion criterion) {
        return new RubricCriterionResponse(
                criterion.getId(),
                criterion.getName(),
                criterion.getDescription(),
                criterion.getMaxScore(),
                criterion.isActive()
        );
    }

    private SectionSummaryResponse toSectionSummary(Section section) {
        List<String> teamNames = teamRepo.findBySectionIdOrderByNameAsc(section.getId()).stream()
                .map(Team::getName)
                .toList();
        Rubric rubric = getRubricEntity(section.getRubricId());
        return new SectionSummaryResponse(
                section.getId(),
                section.getName(),
                section.getStartDate(),
                section.getEndDate(),
                rubric.getName(),
                teamNames,
                getActiveWeekNumbers(section)
        );
    }

    private SectionDetailResponse toSectionDetail(Section section) {
        List<Team> teams = teamRepo.findBySectionIdOrderByNameAsc(section.getId());
        Set<Long> assignedStudents = teams.stream()
                .flatMap(team -> team.getStudentIds().stream())
                .collect(Collectors.toSet());
        Set<Long> assignedInstructors = teams.stream()
                .flatMap(team -> team.getInstructorIds().stream())
                .collect(Collectors.toSet());

        List<UserSummaryResponse> unassignedStudents = section.getStudentIds().stream()
                .filter(id -> !assignedStudents.contains(id))
                .map(this::getUser)
                .sorted(Comparator.comparing(UserAccount::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(UserAccount::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toUserSummary)
                .toList();

        List<UserSummaryResponse> unassignedInstructors = section.getInstructorIds().stream()
                .filter(id -> !assignedInstructors.contains(id))
                .map(this::getUser)
                .sorted(Comparator.comparing(UserAccount::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(UserAccount::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toUserSummary)
                .toList();

        List<StudentInvitationResponse> invitations = invitationRepo.findBySectionIdAndAcceptedFalseOrderBySentAtDesc(section.getId())
                .stream()
                .map(this::toStudentInvitation)
                .toList();

        return new SectionDetailResponse(
                section.getId(),
                section.getName(),
                section.getStartDate(),
                section.getEndDate(),
                toRubricDetail(getRubricEntity(section.getRubricId())),
                getActiveWeekNumbers(section),
                new TreeSet<>(section.getInactiveWeekNumbers()),
                teams.stream().map(this::toTeamSummary).toList(),
                unassignedStudents,
                unassignedInstructors,
                invitations
        );
    }

    private TeamSummaryResponse toTeamSummary(Team team) {
        Section section = getSectionEntity(team.getSectionId());
        return new TeamSummaryResponse(
                team.getId(),
                team.getSectionId(),
                section.getName(),
                team.getName(),
                team.getDescription(),
                team.getWebsiteUrl(),
                team.getStudentIds().stream().map(this::getUser).map(this::fullName).sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                team.getInstructorIds().stream().map(this::getUser).map(this::fullName).sorted(String.CASE_INSENSITIVE_ORDER).toList()
        );
    }

    private TeamDetailResponse toTeamDetail(Team team) {
        Section section = getSectionEntity(team.getSectionId());
        List<UserSummaryResponse> students = team.getStudentIds().stream()
                .map(this::getUser)
                .sorted(Comparator.comparing(UserAccount::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(UserAccount::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toUserSummary)
                .toList();
        List<UserSummaryResponse> instructors = team.getInstructorIds().stream()
                .map(this::getUser)
                .sorted(Comparator.comparing(UserAccount::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(UserAccount::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toUserSummary)
                .toList();
        return new TeamDetailResponse(
                team.getId(),
                team.getSectionId(),
                section.getName(),
                team.getName(),
                team.getDescription(),
                team.getWebsiteUrl(),
                students,
                instructors
        );
    }

    private UserSummaryResponse toUserSummary(UserAccount user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isActive()
        );
    }

    private StudentInvitationResponse toStudentInvitation(StudentInvitation invitation) {
        return new StudentInvitationResponse(
                invitation.getId(),
                invitation.getSectionId(),
                invitation.getEmail(),
                invitation.getSubject(),
                invitation.getSentAt(),
                invitation.isAccepted()
        );
    }

    private Set<Integer> getActiveWeekNumbers(Section section) {
        Set<Integer> weeks = new TreeSet<>(getWeekNumbers(section.getStartDate(), section.getEndDate()));
        weeks.removeAll(section.getInactiveWeekNumbers());
        return weeks;
    }

    private Set<Integer> getWeekNumbers(LocalDate startDate, LocalDate endDate) {
        Set<Integer> weeks = new TreeSet<>();
        LocalDate cursor = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastWeek = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        while (!cursor.isAfter(lastWeek)) {
            weeks.add(cursor.get(WeekFields.ISO.weekOfWeekBasedYear()));
            cursor = cursor.plusWeeks(1);
        }
        return weeks;
    }

    private String fullName(UserAccount user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
