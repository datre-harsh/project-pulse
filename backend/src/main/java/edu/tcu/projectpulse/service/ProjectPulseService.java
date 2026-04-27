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
import edu.tcu.projectpulse.repo.NotificationRepository;
import edu.tcu.projectpulse.repo.InstructorInvitationRepository;
import edu.tcu.projectpulse.repo.RubricCriterionRepository;
import edu.tcu.projectpulse.repo.RubricRepository;
import edu.tcu.projectpulse.repo.SectionRepository;
import edu.tcu.projectpulse.repo.StudentInvitationRepository;
import edu.tcu.projectpulse.repo.TeamRepository;
import edu.tcu.projectpulse.repo.UserAccountRepository;
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
            return customMessage.replace("[Registration link]", buildRegistrationLink(token)).trim();
        }
        return ("Project Pulse has invited " + email + " to join the section.\n\n"
                + "To complete registration, use this link:\n"
                + buildRegistrationLink(token) + "\n\n"
                + "If you need help, contact your course admin.").trim();
    }

    private String buildInstructorInvitationMessage(String token, String customMessage) {
        if (customMessage != null && !customMessage.isBlank()) {
            return customMessage.replace("[Registration link]", buildRegistrationLink(token)).trim();
        }
        return ("Hello,\n\n"
                + "[Name of the Admin] has invited you to join The Peer Evaluation Tool. To complete your registration, please use the link below:\n\n"
                + buildRegistrationLink(token) + "\n\n"
                + "If you have any questions or need assistance, feel free to contact [Admin's email] or our team directly.\n\n"
                + "Please note: This email is not monitored, so do not reply directly to this message.\n\n"
                + "Best regards,\n"
                + "Peer Evaluation Tool Team").trim();
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

    private void createNotification(Long userId, String message) {
        Notification notification = new Notification();
        notification.setId(sequenceGeneratorService.generateSequence(Notification.SEQUENCE_NAME));
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(notification);
    }

    public StudentRegistrationResponse registerStudent(StudentRegistrationRequest request) {
        // Extension 2a: Check if student already has an account
        if (userRepo.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new ApiException("An account with this email already exists");
        }

        // Mock invitation token verification (for now, accept any non-empty token)
        if (request.getInvitationToken() == null || request.getInvitationToken().trim().isEmpty()) {
            throw new ApiException("Invalid invitation token");
        }

        // Create new student account
        UserAccount student = new UserAccount();
        student.setId(sequenceGeneratorService.generateSequence(UserAccount.SEQUENCE_NAME)); // Generate Long ID
        student.setFirstName(request.getFirstName().trim());
        student.setLastName(request.getLastName().trim());
        student.setEmail(request.getEmail().trim().toLowerCase());
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student.setRole(Role.STUDENT);
        student.setActive(true);

        UserAccount savedStudent = userRepo.save(student);

        return new StudentRegistrationResponse(
                savedStudent.getId().toString(),
                savedStudent.getFirstName(),
                savedStudent.getLastName(),
                savedStudent.getEmail(),
                "Student account created successfully"
        );
    }

    public ProfileUpdateResponse updateStudentProfile(Long studentId, ProfileUpdateRequest req) {
        UserAccount student = getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can update their profile through this endpoint");
        }

        // Check if email is already used by another user
        Optional<UserAccount> existingUser = userRepo.findByEmailIgnoreCase(req.email().trim());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(studentId)) {
            throw new ApiException("Email is already in use by another account");
        }

        // Update student information
        student.setFirstName(req.firstName().trim());
        student.setLastName(req.lastName().trim());
        student.setEmail(req.email().trim().toLowerCase());
        
        UserAccount savedStudent = userRepo.save(student);

        return new ProfileUpdateResponse(
                savedStudent.getId().toString(),
                savedStudent.getFirstName(),
                savedStudent.getLastName(),
                savedStudent.getEmail(),
                "Profile updated successfully"
        );
    }
}
