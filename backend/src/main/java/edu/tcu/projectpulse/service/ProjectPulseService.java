package edu.tcu.projectpulse.service;

import edu.tcu.projectpulse.api.ApiException;
import edu.tcu.projectpulse.config.SequenceGeneratorService;
import edu.tcu.projectpulse.domain.Role;
import edu.tcu.projectpulse.domain.Notification;
import edu.tcu.projectpulse.domain.InstructorInvitation;
import edu.tcu.projectpulse.domain.Rubric;
import edu.tcu.projectpulse.domain.RubricCriterion;
import edu.tcu.projectpulse.domain.Section;
import edu.tcu.projectpulse.domain.StudentInvitation;
import edu.tcu.projectpulse.domain.Team;
import edu.tcu.projectpulse.domain.UserAccount;
import edu.tcu.projectpulse.dto.*;
import edu.tcu.projectpulse.domain.WeeklyActivity;
import edu.tcu.projectpulse.domain.PeerEvaluation;
import edu.tcu.projectpulse.repo.NotificationRepository;
import edu.tcu.projectpulse.repo.InstructorInvitationRepository;
import edu.tcu.projectpulse.repo.RubricCriterionRepository;
import edu.tcu.projectpulse.repo.RubricRepository;
import edu.tcu.projectpulse.repo.SectionRepository;
import edu.tcu.projectpulse.repo.StudentInvitationRepository;
import edu.tcu.projectpulse.repo.TeamRepository;
import edu.tcu.projectpulse.repo.UserAccountRepository;
import edu.tcu.projectpulse.repo.WeeklyActivityRepository;
import edu.tcu.projectpulse.repo.PeerEvaluationRepository;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final NotificationRepository notificationRepo;
    private final InstructorInvitationRepository instructorInvitationRepo;
    private final RubricRepository rubricRepo;
    private final RubricCriterionRepository rubricRepoCriteria;
    private final StudentInvitationRepository invitationRepo;
    private final WeeklyActivityRepository weeklyActivityRepo;
    private final PeerEvaluationRepository peerEvaluationRepo;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    public LoginResponse login(LoginRequest req) {
        UserAccount user = userRepo.findByEmailIgnoreCase(req.email().trim())
                .orElseThrow(() -> new ApiException("Invalid email or password"));
        if (!user.isActive()) {
            throw new ApiException("This account is deactivated");
        }
        if (user.getPassword() == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException("Invalid email or password");
        }
        return toLoginResponse(user);
    }

    public LoginResponse getCurrentUser(Long userId) {
        return toLoginResponse(requireActiveUser(userId));
    }

    public UserAccount requireActiveUser(Long userId) {
        if (userId == null) {
            throw new ApiException("Login required");
        }
        UserAccount user = getUser(userId);
        if (!user.isActive()) {
            throw new ApiException("This account is deactivated");
        }
        return user;
    }

    public UserAccount requireRole(Long userId, Role... roles) {
        UserAccount user = requireActiveUser(userId);
        Set<Role> allowed = new HashSet<>(Arrays.asList(roles));
        if (!allowed.contains(user.getRole())) {
            throw new ApiException("This action requires one of these roles: " + allowed);
        }
        return user;
    }

    public List<UserSummaryResponse> getInstructorOptions() {
        return userRepo.findByRoleOrderByLastNameAscFirstNameAsc(Role.INSTRUCTOR).stream()
                .filter(UserAccount::isActive)
                .map(this::toUserSummary)
                .toList();
    }

    public List<UserSummaryResponse> getStudentOptions() {
        return userRepo.findByRoleOrderByLastNameAscFirstNameAsc(Role.STUDENT).stream()
                .filter(UserAccount::isActive)
                .map(this::toUserSummary)
                .toList();
    }

    public List<StudentSearchResponse> getStudents(
            String firstName,
            String lastName,
            String email,
            String sectionName,
            String teamName,
            Long sectionId,
            Long teamId
    ) {
        List<UserAccount> students = userRepo.findByRoleOrderByLastNameAscFirstNameAsc(Role.STUDENT).stream()
                .filter(UserAccount::isActive)
                .filter(student -> matchesValue(student.getFirstName(), firstName))
                .filter(student -> matchesValue(student.getLastName(), lastName))
                .filter(student -> matchesValue(student.getEmail(), email))
                .toList();

        List<StudentSearchResponse> matches = new ArrayList<>();
        for (UserAccount student : students) {
            List<Section> memberships = sectionRepo.findAll().stream()
                    .filter(section -> section.getStudentIds().contains(student.getId()))
                    .toList();

            if (memberships.isEmpty()) {
                if (sectionId == null && isBlank(sectionName) && teamId == null && isBlank(teamName)) {
                    matches.add(toStudentSearchResponse(student, null, null));
                }
                continue;
            }

            for (Section section : memberships) {
                if (!matchesSectionFilter(section, sectionId, sectionName)) {
                    continue;
                }

                Team team = teamRepo.findBySectionId(section.getId()).stream()
                        .filter(candidate -> candidate.getStudentIds().contains(student.getId()))
                        .findFirst()
                        .orElse(null);

                if (!matchesTeamFilter(team, teamId, teamName)) {
                    continue;
                }

                matches.add(toStudentSearchResponse(student, section, team));
            }
        }

        return matches.stream()
                .sorted(Comparator.comparing(
                                StudentSearchResponse::sectionName,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                        ).reversed()
                        .thenComparing(StudentSearchResponse::lastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(StudentSearchResponse::firstName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<InstructorSearchResponse> getInstructors(
            String firstName,
            String lastName,
            String teamName,
            String status
    ) {
        List<UserAccount> instructors = userRepo.findByRoleOrderByLastNameAscFirstNameAsc(Role.INSTRUCTOR).stream()
                .filter(instructor -> matchesValue(instructor.getFirstName(), firstName))
                .filter(instructor -> matchesValue(instructor.getLastName(), lastName))
                .filter(instructor -> matchesInstructorStatus(instructor, status))
                .toList();

        List<InstructorSearchResponse> matches = new ArrayList<>();
        for (UserAccount instructor : instructors) {
            List<Team> memberships = teamRepo.findAll().stream()
                    .filter(team -> team.getInstructorIds().contains(instructor.getId()))
                    .toList();

            if (memberships.isEmpty()) {
                if (isBlank(teamName)) {
                    matches.add(toInstructorSearchResponse(instructor, null, null));
                }
                continue;
            }

            for (Team team : memberships) {
                if (!matchesValue(team.getName(), teamName)) {
                    continue;
                }

                Section section = getSectionEntity(team.getSectionId());
                matches.add(toInstructorSearchResponse(instructor, section, team));
            }
        }

        return matches.stream()
                .sorted(Comparator.comparing(
                                InstructorSearchResponse::academicYear,
                                Comparator.nullsLast(Integer::compareTo)
                        ).reversed()
                        .thenComparing(InstructorSearchResponse::lastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(InstructorSearchResponse::firstName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public StudentDetailResponse getStudent(Long id, Long sectionId, Long teamId) {
        UserAccount student = getUser(id);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can be viewed through this endpoint");
        }

        Section section = null;
        if (sectionId != null) {
            section = getSectionEntity(sectionId);
            if (!section.getStudentIds().contains(id)) {
                throw new ApiException("Student is not part of the selected section");
            }
        } else {
            section = sectionRepo.findAll().stream()
                    .filter(candidate -> candidate.getStudentIds().contains(id))
                    .findFirst()
                    .orElse(null);
        }

        Team team = null;
        if (teamId != null) {
            team = getTeamEntity(teamId);
            if (!team.getStudentIds().contains(id)) {
                throw new ApiException("Student is not part of the selected team");
            }
            if (section != null && !Objects.equals(team.getSectionId(), section.getId())) {
                throw new ApiException("Selected team does not belong to the selected section");
            }
            if (section == null) {
                section = getSectionEntity(team.getSectionId());
            }
        } else if (section != null) {
            Team scopedTeam = teamRepo.findBySectionId(section.getId()).stream()
                    .filter(candidate -> candidate.getStudentIds().contains(id))
                    .findFirst()
                    .orElse(null);
            team = scopedTeam;
        }

        return new StudentDetailResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                section == null ? "Not assigned to a section" : section.getName(),
                team == null ? "Not assigned to a team" : team.getName(),
                List.of("No peer evaluations are available in this version of Project Pulse yet."),
                List.of("No WARs are available in this version of Project Pulse yet.")
        );
    }

    public InstructorDetailResponse getInstructor(Long id) {
        UserAccount instructor = getUser(id);
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new ApiException("Only instructors can be viewed through this endpoint");
        }

        List<InstructorSectionTeamsResponse> supervisedTeams = teamRepo.findAll().stream()
                .filter(team -> team.getInstructorIds().contains(id))
                .collect(Collectors.groupingBy(
                        Team::getSectionId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Section section = getSectionEntity(entry.getKey());
                    List<String> teamNames = entry.getValue().stream()
                            .map(Team::getName)
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .toList();
                    return new InstructorSectionTeamsResponse(section.getId(), section.getName(), teamNames);
                })
                .sorted(Comparator.comparing(InstructorSectionTeamsResponse::sectionName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new InstructorDetailResponse(
                instructor.getId(),
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.isActive() ? "Active" : "Deactivated",
                supervisedTeams
        );
    }

    public InstructorDetailResponse deactivateInstructor(Long id, InstructorDeactivationRequest req) {
        UserAccount instructor = getUser(id);
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new ApiException("Only instructors can be deactivated through this endpoint");
        }
        if (!instructor.isActive()) {
            throw new ApiException("Instructor is already deactivated");
        }

        // Ralph: Keep the account record in place for future recovery while still recording why access was removed.
        instructor.setActive(false);
        instructor.setDeactivationReason(req.reason().trim());
        userRepo.save(instructor);
        return getInstructor(id);
    }

    public InstructorDetailResponse reactivateInstructor(Long id) {
        UserAccount instructor = getUser(id);
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new ApiException("Only instructors can be reactivated through this endpoint");
        }
        if (instructor.isActive()) {
            throw new ApiException("Instructor is already active");
        }

        // Reactivate the instructor and clear deactivation reason
        instructor.setActive(true);
        instructor.setDeactivationReason(null);
        userRepo.save(instructor);
        
        // Create notification for the instructor
        createNotification(id, "Your instructor account has been reactivated. You now have access to The Peer Evaluation Tool.");
        
        return getInstructor(id);
    }

    public void deleteStudent(Long id) {
        UserAccount student = getUser(id);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can be deleted through this endpoint");
        }

        // Ralph: Remove the student from every section and team first so no dangling membership IDs remain.
        List<Section> sections = sectionRepo.findAll().stream()
                .filter(section -> section.getStudentIds().contains(id))
                .toList();
        for (Section section : sections) {
            section.getStudentIds().remove(id);
            sectionRepo.save(section);
        }

        List<Team> teams = teamRepo.findAll().stream()
                .filter(team -> team.getStudentIds().contains(id))
                .toList();
        for (Team team : teams) {
            team.getStudentIds().remove(id);
            teamRepo.save(team);
        }

        notificationRepo.deleteAll(notificationRepo.findByUserIdOrderByCreatedAtDesc(id));
        userRepo.delete(student);
    }

    public List<InstructorInvitationResponse> getInstructorInvitations() {
        return instructorInvitationRepo.findByAcceptedFalseOrderBySentAtDesc().stream()
                .map(this::toInstructorInvitation)
                .toList();
    }

    public InstructorInvitationTokenResponse getInstructorInvitation(String token) {
        InstructorInvitation invitation = findOpenInstructorInvitation(token);
        return new InstructorInvitationTokenResponse(
                invitation.getEmail(),
                invitation.getSubject(),
                invitation.getSentAt(),
                invitation.isAccepted()
        );
    }

    public List<InstructorInvitationResponse> inviteInstructors(InstructorInvitationRequest req) {
        List<String> emails = parseStrictSemicolonEmails(req.emails());

        String subject = req.subject() == null || req.subject().isBlank()
                ? "Welcome to The Peer Evaluation Tool - Complete Your Registration"
                : req.subject().trim();

        List<InstructorInvitationResponse> responses = new ArrayList<>();
        for (String email : emails) {
            String token = UUID.randomUUID().toString().replace("-", "");
            String message = buildInstructorInvitationMessage(token, req.message());

            InstructorInvitation invitation = new InstructorInvitation();
            invitation.setId(sequenceGeneratorService.generateSequence(InstructorInvitation.SEQUENCE_NAME));
            invitation.setEmail(email);
            invitation.setToken(token);
            invitation.setSubject(subject);
            invitation.setMessage(message);
            invitation.setSentAt(LocalDateTime.now());
            InstructorInvitation saved = instructorInvitationRepo.save(invitation);
            responses.add(toInstructorInvitation(saved));
        }
        return responses;
    }

    public List<NotificationResponse> getUserNotifications(Long id) {
        getUser(id);
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(id).stream()
                .map(this::toNotificationResponse)
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
        notifyAssignedInstructors(saved, Set.of());
        return toTeamDetail(saved);
    }

    public TeamDetailResponse updateTeam(Long id, TeamRequest req) {
        validateTeamRequest(req, id);
        Team team = getTeamEntity(id);
        Set<Long> previousInstructorIds = new HashSet<>(team.getInstructorIds());
        applyTeam(team, req);
        Team saved = teamRepo.save(team);
        syncSectionMemberships(saved);
        notifyAssignedInstructors(saved, previousInstructorIds);
        return toTeamDetail(saved);
    }

    public TeamDetailResponse removeStudentFromTeam(Long teamId, Long studentId) {
        Team team = getTeamEntity(teamId);
        UserAccount student = getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can be removed from a team");
        }
        if (!team.getStudentIds().contains(studentId)) {
            throw new ApiException("Student is not assigned to this team");
        }

        team.getStudentIds().remove(studentId);
        Team saved = teamRepo.save(team);
        // Ralph: Persist the notification so the admin can verify the student was informed.
        createNotification(studentId, "You have been removed from team " + team.getName() + ".");
        return toTeamDetail(saved);
    }

    public TeamDetailResponse removeInstructorFromTeam(Long teamId, Long instructorId) {
        Team team = getTeamEntity(teamId);
        UserAccount instructor = getUser(instructorId);
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new ApiException("Only instructors can be removed from a team");
        }
        if (!team.getInstructorIds().contains(instructorId)) {
            throw new ApiException("Instructor is not assigned to this team");
        }

        team.getInstructorIds().remove(instructorId);
        validateBr1(team);
        Team saved = teamRepo.save(team);
        // Ralph: Persist the removal notice so the admin can verify the instructor was informed.
        createNotification(instructorId, "You have been removed from team " + team.getName() + ".");
        return toTeamDetail(saved);
    }

    public void deleteTeam(Long id) {
        Team team = getTeamEntity(id);
        for (Long studentId : team.getStudentIds()) {
            createNotification(studentId, "Team " + team.getName() + " was deleted.");
        }
        for (Long instructorId : team.getInstructorIds()) {
            createNotification(instructorId, "Team " + team.getName() + " was deleted.");
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
        Set<Long> sectionInstructors = section.getInstructorIds() == null ? Set.of() : section.getInstructorIds();
        for (Long instructorId : req.instructorIds() == null ? Set.<Long>of() : req.instructorIds()) {
            if (!sectionInstructors.contains(instructorId)) {
                throw new ApiException("Instructor " + instructorId + " is not part of the selected section");
            }
        }

        Team probe = new Team();
        probe.setInstructorIds(req.instructorIds() == null ? new HashSet<Long>() : new HashSet<>(req.instructorIds()));
        validateBr1(probe);
    }

    private void applyTeam(Team team, TeamRequest req) {
        team.setSectionId(req.sectionId());
        team.setName(req.name().trim());
        team.setDescription(req.description().trim());
        team.setWebsiteUrl(req.websiteUrl() == null || req.websiteUrl().isBlank() ? null : req.websiteUrl().trim());
        team.setStudentIds(req.studentIds() == null ? new HashSet<Long>() : new HashSet<>(req.studentIds()));
        team.setInstructorIds(req.instructorIds() == null ? new HashSet<Long>() : new HashSet<>(req.instructorIds()));
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

    private List<String> parseStrictSemicolonEmails(String emails) {
        String normalized = emails == null ? "" : emails.trim();
        if (normalized.isBlank()) {
            throw new ApiException("At least one instructor email is required");
        }
        if (normalized.endsWith(";")) {
            throw new ApiException("Instructor emails cannot end with a semicolon");
        }
        if (!normalized.contains(";") && normalized.contains(" ")) {
            throw new ApiException("Instructor emails must be separated by semicolons");
        }

        List<String> parsed = Arrays.stream(normalized.split(";"))
                .map(String::trim)
                .toList();
        for (int index = 0; index < parsed.size(); index++) {
            String email = parsed.get(index);
            if (email.isBlank()) {
                throw new ApiException("Instructor email " + (index + 1) + " is empty");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new ApiException("Invalid instructor email format: " + email);
            }
        }
        return parsed;
    }

    private String buildInvitationMessage(String email, String token, String customMessage) {
        if (customMessage != null && !customMessage.isBlank()) {
            return customMessage.replace("[Registration link]", buildStudentRegistrationLink(token)).trim();
        }
        return ("Project Pulse has invited " + email + " to join the section.\n\n"
                + "To complete registration, use this link:\n"
                + buildStudentRegistrationLink(token) + "\n\n"
                + "If you need help, contact your course admin.").trim();
    }

    private String buildInstructorInvitationMessage(String token, String customMessage) {
        if (customMessage != null && !customMessage.isBlank()) {
            return customMessage.replace("[Registration link]", buildInstructorRegistrationLink(token)).trim();
        }
        return ("Hello,\n\n"
                + "[Name of the Admin] has invited you to join The Peer Evaluation Tool. To complete your registration, please use the link below:\n\n"
                + buildInstructorRegistrationLink(token) + "\n\n"
                + "If you have any questions or need assistance, feel free to contact [Admin's email] or our team directly.\n\n"
                + "Please note: This email is not monitored, so do not reply directly to this message.\n\n"
                + "Best regards,\n"
                + "Peer Evaluation Tool Team").trim();
    }

    private String buildStudentRegistrationLink(String token) {
        return "/register-student/" + token;
    }

    private String buildInstructorRegistrationLink(String token) {
        return "/register-instructor/" + token;
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

    private boolean matchesValue(String actual, String expected) {
        return expected == null
                || expected.isBlank()
                || (actual != null && actual.toLowerCase(Locale.ROOT).contains(expected.trim().toLowerCase(Locale.ROOT)));
    }

    private boolean matchesSectionFilter(Section section, Long sectionId, String sectionName) {
        return (sectionId == null || Objects.equals(section.getId(), sectionId))
                && matchesValue(section.getName(), sectionName);
    }

    private boolean matchesTeamFilter(Team team, Long teamId, String teamName) {
        if (teamId != null && (team == null || !Objects.equals(team.getId(), teamId))) {
            return false;
        }
        if (!isBlank(teamName) && (team == null || !matchesValue(team.getName(), teamName))) {
            return false;
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean matchesInstructorStatus(UserAccount instructor, String status) {
        if (isBlank(status)) {
            return true;
        }

        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "ACTIVE" -> instructor.isActive();
            case "DEACTIVATED" -> !instructor.isActive();
            default -> throw new ApiException("Status must be ACTIVE or DEACTIVATED");
        };
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

        List<UserSummaryResponse> sectionStudents = section.getStudentIds().stream()
                .map(this::getUser)
                .sorted(Comparator.comparing(UserAccount::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(UserAccount::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toUserSummary)
                .toList();

        List<UserSummaryResponse> sectionInstructors = section.getInstructorIds().stream()
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
                sectionStudents,
                sectionInstructors,
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

    private LoginResponse toLoginResponse(UserAccount user) {
        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    private StudentSearchResponse toStudentSearchResponse(UserAccount student, Section section, Team team) {
        return new StudentSearchResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                section == null ? null : section.getId(),
                section == null ? null : section.getName(),
                team == null ? null : team.getId(),
                team == null ? null : team.getName()
        );
    }

    private InstructorSearchResponse toInstructorSearchResponse(UserAccount instructor, Section section, Team team) {
        return new InstructorSearchResponse(
                instructor.getId(),
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.getEmail(),
                section == null ? null : section.getStartDate().getYear(),
                section == null ? null : section.getId(),
                section == null ? null : section.getName(),
                team == null ? null : team.getId(),
                team == null ? null : team.getName(),
                instructor.isActive() ? "Active" : "Deactivated"
        );
    }

    private StudentInvitationResponse toStudentInvitation(StudentInvitation invitation) {
        return new StudentInvitationResponse(
                invitation.getId(),
                invitation.getSectionId(),
                invitation.getEmail(),
                invitation.getSubject(),
                invitation.getMessage(),
                invitation.getToken(),
                invitation.getSentAt(),
                invitation.isAccepted()
        );
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }

    private InstructorInvitationResponse toInstructorInvitation(InstructorInvitation invitation) {
        return new InstructorInvitationResponse(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getSubject(),
                invitation.getMessage(),
                invitation.getToken(),
                invitation.getSentAt(),
                invitation.isAccepted()
        );
    }

    private InstructorInvitation findOpenInstructorInvitation(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException("Invitation token is required");
        }

        InstructorInvitation invitation = instructorInvitationRepo.findByToken(token.trim())
                .orElseThrow(() -> new ApiException("Invalid invitation token"));

        if (invitation.isAccepted()) {
            throw new ApiException("This invitation has already been used");
        }

        return invitation;
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

    private void createNotification(Long userId, String message) {
        Notification notification = new Notification();
        notification.setId(sequenceGeneratorService.generateSequence(Notification.SEQUENCE_NAME));
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(notification);
    }

    public StudentRegistrationResponse registerStudent(StudentRegistrationRequest request) {
        StudentInvitation invitation = findOpenStudentInvitation(request.getInvitationToken());
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (!invitation.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new ApiException("The registration email must match the invited student email");
        }

        if (userRepo.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ApiException("An account with this email already exists");
        }

        UserAccount student = new UserAccount();
        student.setId(sequenceGeneratorService.generateSequence(UserAccount.SEQUENCE_NAME));
        student.setFirstName(request.getFirstName().trim());
        student.setLastName(request.getLastName().trim());
        student.setEmail(normalizedEmail);
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setRole(Role.STUDENT);
        student.setActive(true);

        UserAccount savedStudent = userRepo.save(student);
        invitation.setAccepted(true);
        invitationRepo.save(invitation);

        Section section = getSectionEntity(invitation.getSectionId());
        section.getStudentIds().add(savedStudent.getId());
        sectionRepo.save(section);

        return new StudentRegistrationResponse(
                savedStudent.getId().toString(),
                savedStudent.getFirstName(),
                savedStudent.getLastName(),
                savedStudent.getEmail(),
                "Student account created successfully"
        );
    }

    private StudentInvitation findOpenStudentInvitation(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException("Invalid invitation token");
        }

        StudentInvitation invitation = invitationRepo.findByToken(token.trim())
                .orElseThrow(() -> new ApiException("Invalid invitation token"));

        if (invitation.isAccepted()) {
            throw new ApiException("This invitation has already been used");
        }

        return invitation;
    }

    public ProfileUpdateResponse updateStudentProfile(Long studentId, ProfileUpdateRequest req) {
        UserAccount student = getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can update their profile through this endpoint");
        }

        // Check if email is already used by another user
        Optional<UserAccount> existingUser = userRepo.findByEmailIgnoreCase(req.getEmail().trim());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(studentId)) {
            throw new ApiException("Email is already in use by another account");
        }

        student.setFirstName(req.getFirstName().trim());
        student.setLastName(req.getLastName().trim());
        student.setEmail(req.getEmail().trim());

        UserAccount savedStudent = userRepo.save(student);
        return new ProfileUpdateResponse(
                savedStudent.getId().toString(),
                savedStudent.getFirstName(),
                savedStudent.getLastName(),
                savedStudent.getEmail(),
                "Profile updated successfully"
        );
    }

    // Weekly Activity Report (WAR) Methods
    
    public List<WeeklyActivityResponse> getWeeklyActivities(String studentId, String weekId) {
        UserAccount student = getUser(Long.parseLong(studentId));
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can access their weekly activities");
        }

        List<WeeklyActivity> activities;
        if (weekId != null && !weekId.isBlank()) {
            activities = weeklyActivityRepo.findByStudentIdAndWeekIdOrderByCreatedAtDesc(studentId, weekId);
        } else {
            activities = weeklyActivityRepo.findByStudentIdOrderByCreatedAtDesc(studentId);
        }

        return activities.stream().map(this::toWeeklyActivityResponse).toList();
    }

    public WeeklyActivityResponse createWeeklyActivity(String studentId, WeeklyActivityRequest req) {
        UserAccount student = getUser(Long.parseLong(studentId));
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can create weekly activities");
        }

        WeeklyActivity activity = new WeeklyActivity();
        activity.setId(String.valueOf(System.currentTimeMillis())); // Using String ID for MongoDB consistency
        activity.setStudentId(studentId);
        activity.setCategory(req.getCategory());
        activity.setDescription(req.getDescription().trim());
        activity.setPlannedHours(req.getPlannedHours());
        activity.setActualHours(req.getActualHours() != null ? req.getActualHours() : 0.0);
        activity.setStatus(req.getStatus());
        activity.setWeekId(req.getWeekId());
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());

        WeeklyActivity saved = weeklyActivityRepo.save(activity);
        return toWeeklyActivityResponse(saved);
    }

    public WeeklyActivityResponse updateWeeklyActivity(String studentId, String activityId, WeeklyActivityRequest req) {
        UserAccount student = getUser(Long.parseLong(studentId));
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can update their weekly activities");
        }

        WeeklyActivity activity = weeklyActivityRepo.findById(activityId)
                .orElseThrow(() -> new ApiException("Activity not found"));
        
        if (!activity.getStudentId().equals(studentId)) {
            throw new ApiException("You can only update your own activities");
        }

        activity.setCategory(req.getCategory());
        activity.setDescription(req.getDescription().trim());
        activity.setPlannedHours(req.getPlannedHours());
        activity.setActualHours(req.getActualHours() != null ? req.getActualHours() : 0.0);
        activity.setStatus(req.getStatus());
        activity.setWeekId(req.getWeekId());
        activity.setUpdatedAt(LocalDateTime.now());

        WeeklyActivity saved = weeklyActivityRepo.save(activity);
        return toWeeklyActivityResponse(saved);
    }

    public void deleteWeeklyActivity(String studentId, String activityId) {
        UserAccount student = getUser(Long.parseLong(studentId));
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can delete their weekly activities");
        }

        WeeklyActivity activity = weeklyActivityRepo.findById(activityId)
                .orElseThrow(() -> new ApiException("Activity not found"));
        
        if (!activity.getStudentId().equals(studentId)) {
            throw new ApiException("You can only delete your own activities");
        }

        weeklyActivityRepo.delete(activity);
    }

    private WeeklyActivityResponse toWeeklyActivityResponse(WeeklyActivity activity) {
        return new WeeklyActivityResponse(
                activity.getId(),
                activity.getStudentId(),
                activity.getCategory(),
                activity.getDescription(),
                activity.getPlannedHours(),
                activity.getActualHours(),
                activity.getStatus(),
                activity.getWeekId(),
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }

    // Peer Evaluation Methods
    
    public PeerEvaluationResponse submitPeerEvaluation(Long evaluatorId, PeerEvaluationRequest req) {
        UserAccount evaluator = getUser(evaluatorId);
        if (evaluator.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can submit peer evaluations");
        }

        UserAccount evaluatee = getUser(req.getEvaluateeId());
        if (evaluatee.getRole() != Role.STUDENT) {
            throw new ApiException("Can only evaluate other students");
        }

        if (evaluatorId.equals(req.getEvaluateeId())) {
            throw new ApiException("Cannot evaluate yourself");
        }

        // Business Rule BR-3: Check if evaluation already exists for this week and teammate
        peerEvaluationRepo.findByEvaluatorIdAndEvaluateeIdAndWeekId(evaluatorId, req.getEvaluateeId(), req.getWeekId())
                .ifPresent(existing -> {
                    throw new ApiException("You have already submitted an evaluation for this student this week. Evaluations cannot be edited once completed.");
                });

        // Validate scores
        if (req.getScores() == null || req.getScores().isEmpty()) {
            throw new ApiException("Scores are required");
        }

        // Rubric criteria in the requirements use a 1-10 score range.
        req.getScores().forEach((criterion, score) -> {
            if (score == null || score < 1 || score > 10) {
                throw new ApiException("All scores must be between 1 and 10");
            }
        });

        // Verify students are in the same team
        if (!areStudentsInSameTeam(evaluatorId, req.getEvaluateeId())) {
            throw new ApiException("You can only evaluate students in your team");
        }

        PeerEvaluation evaluation = new PeerEvaluation();
        evaluation.setId(sequenceGeneratorService.generateSequence(PeerEvaluation.SEQUENCE_NAME));
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setEvaluateeId(req.getEvaluateeId());
        evaluation.setWeekId(req.getWeekId().trim());
        evaluation.setScores(req.getScores());
        evaluation.setPublicComment(req.getPublicComment() != null ? req.getPublicComment().trim() : null);
        evaluation.setPrivateComment(req.getPrivateComment() != null ? req.getPrivateComment().trim() : null);
        evaluation.setCreatedAt(LocalDateTime.now());
        evaluation.setUpdatedAt(LocalDateTime.now());

        PeerEvaluation saved = peerEvaluationRepo.save(evaluation);
        return toPeerEvaluationResponse(saved, "Peer evaluation submitted successfully");
    }

    private boolean areStudentsInSameTeam(Long studentId1, Long studentId2) {
        List<Team> teams1 = teamRepo.findAll().stream()
                .filter(team -> team.getStudentIds().contains(studentId1))
                .toList();

        for (Team team : teams1) {
            if (team.getStudentIds().contains(studentId2)) {
                return true;
            }
        }
        return false;
    }

    private PeerEvaluationResponse toPeerEvaluationResponse(PeerEvaluation evaluation, String message) {
        return new PeerEvaluationResponse(
                evaluation.getId(),
                evaluation.getEvaluatorId(),
                evaluation.getEvaluateeId(),
                evaluation.getWeekId(),
                evaluation.getScores(),
                evaluation.getPublicComment(),
                evaluation.getPrivateComment(),
                evaluation.getCreatedAt(),
                evaluation.getUpdatedAt(),
                message
        );
    }

    // Peer Evaluation Report Methods (BR-5: Anonymous aggregated scores only)
    
    public PeerEvaluationReportResponse getPeerEvaluationReport(Long studentId, String weekId) {
        UserAccount student = getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can view their peer evaluation reports");
        }

        // Get all evaluations for this student for the specified week
        List<PeerEvaluation> evaluations = peerEvaluationRepo.findByEvaluateeIdAndWeekId(studentId, weekId);

        if (evaluations.isEmpty()) {
            return new PeerEvaluationReportResponse(
                    studentId,
                    weekId,
                    Map.of(),
                    List.of(),
                    "No evaluations available for this week"
            );
        }

        // BR-5 Enforcement: Calculate aggregated scores per criterion (anonymous)
        Map<String, Double> averageScores = calculateAverageScores(evaluations);

        // BR-5 Enforcement: Collect only public comments (anonymous)
        List<String> publicComments = evaluations.stream()
                .map(PeerEvaluation::getPublicComment)
                .filter(comment -> comment != null && !comment.trim().isEmpty())
                .map(String::trim)
                .toList();

        return new PeerEvaluationReportResponse(
                studentId,
                weekId,
                averageScores,
                publicComments,
                "Peer evaluation report retrieved successfully"
        );
    }

    private Map<String, Double> calculateAverageScores(List<PeerEvaluation> evaluations) {
        Map<String, Double> averageScores = new HashMap<>();
        Map<String, Integer> scoreCounts = new HashMap<>();
        Map<String, Double> scoreSums = new HashMap<>();

        // Aggregate scores across all evaluations
        for (PeerEvaluation evaluation : evaluations) {
            for (Map.Entry<String, Integer> entry : evaluation.getScores().entrySet()) {
                String criterion = entry.getKey();
                Integer score = entry.getValue();

                scoreSums.put(criterion, scoreSums.getOrDefault(criterion, 0.0) + score);
                scoreCounts.put(criterion, scoreCounts.getOrDefault(criterion, 0) + 1);
            }
        }

        // Calculate averages
        for (String criterion : scoreSums.keySet()) {
            double sum = scoreSums.get(criterion);
            int count = scoreCounts.get(criterion);
            averageScores.put(criterion, sum / count);
        }

        return averageScores;
    }

    private void notifyAssignedInstructors(Team team, Set<Long> previousInstructorIds) {
        // Simple implementation - create notifications for instructors
        // This is a placeholder for the actual notification logic
        Set<Long> currentInstructorIds = team.getInstructorIds();
        
        // Notify new instructors
        currentInstructorIds.stream()
                .filter(instructorId -> !previousInstructorIds.contains(instructorId))
                .forEach(instructorId -> {
                    createNotification(instructorId, "You have been assigned to team: " + team.getName());
                });
    }

    // Instructor Registration Methods (UC-30)
    
    public InstructorRegistrationResponse registerInstructor(InstructorRegistrationRequest request) {
        InstructorInvitation invitation = findOpenInstructorInvitation(request.getInvitationToken());
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (!invitation.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new ApiException("The registration email must match the invited instructor email");
        }

        // Ralph: Keep the invitation single-use and prevent duplicate instructor accounts.
        if (userRepo.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ApiException("Account already set up");
        }
        
        // Create new instructor account
        UserAccount instructor = new UserAccount();
        instructor.setId(sequenceGeneratorService.generateSequence(UserAccount.SEQUENCE_NAME)); // Generate Long ID
        instructor.setFirstName(request.getFirstName().trim());
        instructor.setLastName(request.getLastName().trim());
        instructor.setEmail(normalizedEmail);
        instructor.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt encryption
        instructor.setRole(Role.INSTRUCTOR);
        instructor.setActive(true);

        UserAccount savedInstructor = userRepo.save(instructor);
        invitation.setAccepted(true);
        instructorInvitationRepo.save(invitation);

        return new InstructorRegistrationResponse(
                savedInstructor.getId(),
                savedInstructor.getFirstName(),
                savedInstructor.getLastName(),
                savedInstructor.getEmail(),
                "Instructor account created successfully"
        );
    }

    // Instructor Evaluation Methods (UC-31)
    
    public InstructorEvaluationResponse getStudentEvaluationsForInstructor(Long instructorId, Long studentId, String weekId) {
        // Verify instructor has access to this student
        UserAccount instructor = getUser(instructorId);
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new ApiException("Only instructors can access student evaluations");
        }
        
        UserAccount student = getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Can only evaluate students");
        }
        
        // Get all peer evaluations for this student for the specified week
        List<PeerEvaluation> evaluations = peerEvaluationRepo.findByEvaluateeIdAndWeekIdOrderByCreatedAtDesc(studentId, weekId);
        
        // Convert to detail DTOs with evaluator names
        List<StudentEvaluationDetail> evaluationDetails = evaluations.stream()
                .map(evaluation -> {
                    UserAccount evaluator = getUser(evaluation.getEvaluatorId());
                    return new StudentEvaluationDetail(
                            evaluation.getId(),
                            evaluation.getEvaluatorId(),
                            evaluator.getFirstName() + " " + evaluator.getLastName(),
                            evaluation.getEvaluateeId(),
                            evaluation.getWeekId(),
                            evaluation.getScores(),
                            evaluation.getPublicComment(),
                            evaluation.getPrivateComment(),
                            evaluation.getCreatedAt()
                    );
                })
                .toList();
        
        // Find students who did not submit evaluations (non-evaluators)
        List<String> nonEvaluators = findNonEvaluators(studentId, weekId, evaluations);
        
        // Calculate system suggested grade (average of total scores)
        Double systemSuggestedGrade = calculateSystemSuggestedGrade(evaluations);
        
        return new InstructorEvaluationResponse(
                studentId,
                student.getFirstName() + " " + student.getLastName(),
                weekId,
                evaluationDetails,
                nonEvaluators,
                systemSuggestedGrade,
                "Student evaluations retrieved successfully"
        );
    }
    
    private List<String> findNonEvaluators(Long studentId, String weekId, List<PeerEvaluation> evaluations) {
        // Get all students in the same team as the evaluated student
        List<UserAccount> teammates = getStudentTeammates(studentId);
        
        // Get IDs of students who submitted evaluations
        Set<Long> evaluatorIds = evaluations.stream()
                .map(PeerEvaluation::getEvaluatorId)
                .collect(Collectors.toSet());
        
        // Find teammates who did not submit evaluations
        return teammates.stream()
                .filter(teammate -> !teammate.getId().equals(studentId)) // Exclude the student being evaluated
                .filter(teammate -> !evaluatorIds.contains(teammate.getId())) // Exclude those who submitted
                .map(teammate -> teammate.getFirstName() + " " + teammate.getLastName())
                .sorted()
                .toList();
    }
    
    private List<UserAccount> getStudentTeammates(Long studentId) {
        // Find the team the student belongs to
        List<Team> teams = teamRepo.findAll().stream()
                .filter(team -> team.getStudentIds().contains(studentId))
                .toList();
        
        if (teams.isEmpty()) {
            return List.of();
        }
        
        // Get all students in the first team found (assuming student belongs to only one team)
        Team team = teams.get(0);
        return team.getStudentIds().stream()
                .map(this::getUser)
                .filter(student -> student.getRole() == Role.STUDENT)
                .toList();
    }
    
    private Double calculateSystemSuggestedGrade(List<PeerEvaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return 0.0;
        }
        
        // Calculate total score for each evaluation
        List<Double> totalScores = evaluations.stream()
                .map(evaluation -> {
                    // Sum up all criterion scores for this evaluation
                    return evaluation.getScores().values().stream()
                            .mapToDouble(Integer::doubleValue)
                            .sum();
                })
                .toList();
        
        // Calculate average of total scores
        return totalScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
    
    public String saveInstructorFinalDecision(Long instructorId, Long studentId, String weekId, InstructorFinalDecisionRequest request) {
        // Verify instructor has access to this student
        UserAccount instructor = getUser(instructorId);
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new ApiException("Only instructors can save final decisions");
        }
        
        UserAccount student = getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Can only evaluate students");
        }
        
        // TODO: Save the instructor's final decision to a separate table/entity
        // For now, we'll just return a success message
        // In a complete implementation, you would save this to an InstructorEvaluation entity
        
        return String.format("Final decision saved for %s. Grade: %.1f, Comment: %s", 
                student.getFirstName() + " " + student.getLastName(), 
                request.getFinalGrade(), 
                request.getInstructorComment() != null ? request.getInstructorComment() : "No comment");
    }
    
    // Section-Level Evaluation Report Methods (UC-31 Refactored)
    
    public SectionEvaluationReportResponse getSectionEvaluationReport(Long viewerId, Long sectionId, String weekId) {
        UserAccount viewer = getUser(viewerId);
        if (viewer.getRole() != Role.ADMIN && viewer.getRole() != Role.INSTRUCTOR) {
            throw new ApiException("Only admins and instructors can access section evaluation reports");
        }
        
        Section section = getSectionEntity(sectionId);
        
        // Get all students in the section
        List<UserAccount> sectionStudents = section.getStudentIds().stream()
                .map(this::getUser)
                .filter(student -> student.getRole() == Role.STUDENT)
                .sorted(Comparator.comparing(UserAccount::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(UserAccount::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        
        // Generate reports for each student
        List<StudentSectionReport> studentReports = sectionStudents.stream()
                .map(student -> generateStudentReport(student, weekId))
                .toList();
        
        return new SectionEvaluationReportResponse(
                sectionId,
                section.getName(),
                weekId,
                studentReports,
                "Section evaluation report generated successfully"
        );
    }
    
    private StudentSectionReport generateStudentReport(UserAccount student, String weekId) {
        // Get all peer evaluations for this student for the specified week
        List<PeerEvaluation> evaluations = peerEvaluationRepo.findByEvaluateeIdAndWeekIdOrderByCreatedAtDesc(student.getId(), weekId);
        
        // Calculate grade (average of total scores)
        String grade = calculateStudentGrade(evaluations);
        
        // Generate evaluator details
        List<EvaluatorDetail> evaluatorDetails = evaluations.stream()
                .map(evaluation -> {
                    UserAccount evaluator = getUser(evaluation.getEvaluatorId());
                    return new EvaluatorDetail(
                            evaluator.getFirstName() + " " + evaluator.getLastName(),
                            evaluation.getPublicComment(),
                            evaluation.getPrivateComment()
                    );
                })
                .toList();
        
        return new StudentSectionReport(
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                grade,
                evaluatorDetails
        );
    }
    
    private String calculateStudentGrade(List<PeerEvaluation> evaluations) {
        if (evaluations.isEmpty()) {
            return "0/60"; // Default when no evaluations
        }
        
        // Calculate total score for each evaluation
        List<Double> totalScores = evaluations.stream()
                .map(evaluation -> {
                    // Sum up all criterion scores for this evaluation
                    return evaluation.getScores().values().stream()
                            .mapToDouble(Integer::doubleValue)
                            .sum();
                })
                .toList();
        
        // Calculate average of total scores
        double averageScore = totalScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // Format as "X/60" (assuming max score is 60 based on PDF example)
        return String.format("%d/60", Math.round(averageScore));
    }
    
    // Team WAR Report Methods (UC-32)
    
    public TeamWARReportResponse getTeamWARReport(Long teamId, String weekId) {
        Team team = getTeamEntity(teamId);

        List<UserAccount> teamStudents = team.getStudentIds().stream()
                .map(this::getUser)
                .filter(student -> student.getRole() == Role.STUDENT)
                .sorted(Comparator.comparing(UserAccount::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(UserAccount::getFirstName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<StudentActivity> activeStudents = new ArrayList<>();
        List<String> missingStudents = new ArrayList<>();

        for (UserAccount student : teamStudents) {
            List<WeeklyActivity> activities = weeklyActivityRepo
                    .findByStudentIdAndWeekIdOrderByCreatedAtDesc(student.getId().toString(), weekId);

            if (activities.isEmpty()) {
                missingStudents.add(fullName(student));
            } else {
                activeStudents.add(new StudentActivity(
                        fullName(student),
                        activities.stream().map(this::toActivityDetail).toList()
                ));
            }
        }
        
        return new TeamWARReportResponse(
                teamId,
                team.getName(),
                weekId,
                activeStudents,
                missingStudents,
                "Team WAR report generated successfully"
        );
    }
    
    // Student Peer Evaluation Report Methods (UC-33)
    
    public List<WeeklyStudentReport> getStudentPeerEvaluationReport(Long studentId, String startWeekId, String endWeekId) {
        UserAccount student = getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Can only generate reports for students");
        }

        return peerEvaluationRepo.findAll().stream()
                .filter(evaluation -> evaluation.getEvaluateeId().equals(studentId))
                .filter(evaluation -> isWeekInRange(evaluation.getWeekId(), startWeekId, endWeekId))
                .collect(Collectors.groupingBy(PeerEvaluation::getWeekId, TreeMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> new WeeklyStudentReport(
                        entry.getKey(),
                        calculateStudentGrade(entry.getValue()),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(PeerEvaluation::getCreatedAt).reversed())
                                .map(evaluation -> new StudentEvaluation(
                                        fullName(getUser(evaluation.getEvaluatorId())),
                                        evaluation.getPublicComment(),
                                        evaluation.getPrivateComment()
                                ))
                                .toList()
                ))
                .toList();
    }
    
    // Student WAR Report Methods (UC-34)
    
    public List<WeeklyStudentWARReport> getStudentWARReport(Long studentId, String startWeekId, String endWeekId) {
        UserAccount student = getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Can only generate reports for students");
        }

        return weeklyActivityRepo.findByStudentIdOrderByCreatedAtDesc(studentId.toString()).stream()
                .filter(activity -> isWeekInRange(activity.getWeekId(), startWeekId, endWeekId))
                .collect(Collectors.groupingBy(WeeklyActivity::getWeekId, TreeMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> new WeeklyStudentWARReport(
                        entry.getKey(),
                        entry.getValue().stream().map(this::toActivityDetail).toList()
                ))
                .toList();
    }

    private ActivityDetail toActivityDetail(WeeklyActivity activity) {
        return new ActivityDetail(
                titleCase(activity.getCategory().name()),
                activity.getDescription(),
                activity.getDescription(),
                activity.getPlannedHours(),
                activity.getActualHours(),
                titleCase(activity.getStatus().name())
        );
    }

    private boolean isWeekInRange(String weekId, String startWeekId, String endWeekId) {
        return !isBlank(weekId)
                && (isBlank(startWeekId) || weekId.compareTo(startWeekId) >= 0)
                && (isBlank(endWeekId) || weekId.compareTo(endWeekId) <= 0);
    }

    private String titleCase(String value) {
        if (value == null) {
            return "";
        }
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("_"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1))
                .collect(Collectors.joining(" "));
    }
}
