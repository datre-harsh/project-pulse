package edu.tcu.projectpulse.api;

import edu.tcu.projectpulse.domain.*;
import edu.tcu.projectpulse.dto.*;
import edu.tcu.projectpulse.service.ProjectPulseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectPulseController {

    private final ProjectPulseService service;

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @GetMapping("/auth/me")
    public LoginResponse getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        return service.getCurrentUser(userId);
    }

    @GetMapping("/rubrics")
    public List<RubricDetailResponse> getRubrics(@RequestParam(required = false) String name) {
        return service.getRubrics(name);
    }

    @GetMapping("/rubrics/{id}")
    public RubricDetailResponse getRubric(@PathVariable Long id) {
        return service.getRubric(id);
    }

    @PostMapping("/rubrics")
    public RubricDetailResponse createRubric(@Valid @RequestBody RubricRequest req) {
        return service.createRubric(req);
    }

    @PutMapping("/rubrics/{id}")
    public RubricDetailResponse updateRubric(@PathVariable Long id, @Valid @RequestBody RubricRequest req) {
        return service.updateRubric(id, req);
    }

    @GetMapping("/options/instructors")
    public List<UserSummaryResponse> getInstructorOptions() {
        return service.getInstructorOptions();
    }

    @GetMapping("/options/students")
    public List<UserSummaryResponse> getStudentOptions() {
        return service.getStudentOptions();
    }

    @GetMapping("/students")
    public List<StudentSearchResponse> getStudents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String sectionName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long teamId
    ) {
        return service.getStudents(firstName, lastName, email, sectionName, teamName, sectionId, teamId);
    }

    @GetMapping("/instructors")
    public List<InstructorSearchResponse> getInstructors(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) String status
    ) {
        return service.getInstructors(firstName, lastName, teamName, status);
    }

    @GetMapping("/instructors/{id}")
    public InstructorDetailResponse getInstructor(@PathVariable Long id) {
        return service.getInstructor(id);
    }

    @PutMapping("/instructors/{id}/deactivate")
    public InstructorDetailResponse deactivateInstructor(@PathVariable Long id, @Valid @RequestBody InstructorDeactivationRequest req) {
        return service.deactivateInstructor(id, req);
    }

    @PutMapping("/instructors/{id}/reactivate")
    public InstructorDetailResponse reactivateInstructor(@PathVariable Long id) {
        return service.reactivateInstructor(id);
    }

    @GetMapping("/students/{id}")
    public StudentDetailResponse getStudent(
            @PathVariable Long id,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long teamId
    ) {
        return service.getStudent(id, sectionId, teamId);
    }

    @DeleteMapping("/students/{id}")
    public void deleteStudent(@PathVariable Long id) {
        service.deleteStudent(id);
    }

    @GetMapping("/instructor-invitations")
    public List<InstructorInvitationResponse> getInstructorInvitations() {
        return service.getInstructorInvitations();
    }

    @GetMapping("/instructor-invitations/token/{token}")
    public InstructorInvitationTokenResponse getInstructorInvitation(@PathVariable String token) {
        return service.getInstructorInvitation(token);
    }

    @PostMapping("/instructor-invitations")
    public List<InstructorInvitationResponse> inviteInstructors(@Valid @RequestBody InstructorInvitationRequest req) {
        return service.inviteInstructors(req);
    }

    @PostMapping("/students/register")
    public StudentRegistrationResponse registerStudent(@Valid @RequestBody StudentRegistrationRequest req) {
        return service.registerStudent(req);
    }

    @GetMapping("/students/profile")
    public ProfileUpdateResponse getStudentProfile(@RequestHeader("X-User-Id") Long userId) {
        UserAccount student = service.requireRole(userId, Role.STUDENT);
        return new ProfileUpdateResponse(
                student.getId().toString(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                "Profile loaded successfully"
        );
    }

    @PutMapping("/students/profile")
    public ProfileUpdateResponse updateStudentProfile(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody ProfileUpdateRequest req) {
        UserAccount student = service.requireRole(userId, Role.STUDENT);
        return service.updateStudentProfile(student.getId(), req);
    }

    // Weekly Activity Report (WAR) Endpoints
    
    @GetMapping("/students/war")
    public List<WeeklyActivityResponse> getWeeklyActivities(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String weekId) {
        String studentId = service.requireRole(userId, Role.STUDENT).getId().toString();
        return service.getWeeklyActivities(studentId, weekId);
    }

    @PostMapping("/students/war")
    public WeeklyActivityResponse createWeeklyActivity(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody WeeklyActivityRequest req) {
        String studentId = service.requireRole(userId, Role.STUDENT).getId().toString();
        return service.createWeeklyActivity(studentId, req);
    }

    @PutMapping("/students/war/{activityId}")
    public WeeklyActivityResponse updateWeeklyActivity(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String activityId, 
            @Valid @RequestBody WeeklyActivityRequest req) {
        String studentId = service.requireRole(userId, Role.STUDENT).getId().toString();
        return service.updateWeeklyActivity(studentId, activityId, req);
    }

    @DeleteMapping("/students/war/{activityId}")
    public void deleteWeeklyActivity(@RequestHeader("X-User-Id") Long userId, @PathVariable String activityId) {
        String studentId = service.requireRole(userId, Role.STUDENT).getId().toString();
        service.deleteWeeklyActivity(studentId, activityId);
    }

    // Peer Evaluation Endpoints
    
    @PostMapping("/students/peer-evaluation")
    public PeerEvaluationResponse submitPeerEvaluation(@RequestHeader("X-User-Id") Long userId, @Valid @RequestBody PeerEvaluationRequest req) {
        Long evaluatorId = service.requireRole(userId, Role.STUDENT).getId();
        return service.submitPeerEvaluation(evaluatorId, req);
    }

    @GetMapping("/students/peer-evaluations/report/{weekId}")
    public PeerEvaluationReportResponse getPeerEvaluationReport(@RequestHeader("X-User-Id") Long userId, @PathVariable String weekId) {
        Long studentId = service.requireRole(userId, Role.STUDENT).getId();
        return service.getPeerEvaluationReport(studentId, weekId);
    }

    // Instructor Registration Endpoints (UC-30)
    
    @PostMapping("/instructors/register")
    public InstructorRegistrationResponse registerInstructor(@Valid @RequestBody InstructorRegistrationRequest request) {
        return service.registerInstructor(request);
    }

    // Instructor Evaluation Endpoints (UC-31 Refactored)
    
    @GetMapping("/instructors/sections/{sectionId}/evaluations/{weekId}")
    public SectionEvaluationReportResponse getSectionEvaluationReport(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sectionId,
            @PathVariable String weekId) {
        Long viewerId = service.requireRole(userId, Role.ADMIN, Role.INSTRUCTOR).getId();
        return service.getSectionEvaluationReport(viewerId, sectionId, weekId);
    }

    // Team WAR Report Endpoints (UC-32)
    
    @GetMapping("/teams/{teamId}/war-report/{weekId}")
    public TeamWARReportResponse getTeamWARReport(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long teamId,
            @PathVariable String weekId) {
        service.requireRole(userId, Role.ADMIN, Role.INSTRUCTOR, Role.STUDENT);
        return service.getTeamWARReport(teamId, weekId);
    }

    // Student Peer Evaluation Report Endpoints (UC-33)
    
    @GetMapping("/students/{studentId}/peer-evaluation-report")
    public List<WeeklyStudentReport> getStudentPeerEvaluationReport(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long studentId, 
            @RequestParam String startWeekId, 
            @RequestParam String endWeekId) {
        service.requireRole(userId, Role.ADMIN, Role.INSTRUCTOR);
        return service.getStudentPeerEvaluationReport(studentId, startWeekId, endWeekId);
    }

    // Student WAR Report Endpoints (UC-34)
    
    @GetMapping("/students/{studentId}/war-report")
    public List<WeeklyStudentWARReport> getStudentWARReport(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long studentId, 
            @RequestParam String startWeekId, 
            @RequestParam String endWeekId) {
        service.requireRole(userId, Role.ADMIN, Role.INSTRUCTOR);
        return service.getStudentWARReport(studentId, startWeekId, endWeekId);
    }

    @GetMapping("/users/{id}/notifications")
    public List<NotificationResponse> getUserNotifications(@PathVariable Long id) {
        return service.getUserNotifications(id);
    }

    @GetMapping("/sections")
    public List<SectionSummaryResponse> getSections(@RequestParam(required = false) String name) {
        return service.getSections(name);
    }

    @GetMapping("/sections/{id}")
    public SectionDetailResponse getSection(@PathVariable Long id) {
        return service.getSection(id);
    }

    @PostMapping("/sections")
    public SectionDetailResponse createSection(@Valid @RequestBody SectionRequest req) {
        return service.createSection(req);
    }

    @PutMapping("/sections/{id}")
    public SectionDetailResponse updateSection(@PathVariable Long id, @Valid @RequestBody SectionRequest req) {
        return service.updateSection(id, req);
    }

    @PutMapping("/sections/{id}/active-weeks")
    public SectionDetailResponse updateActiveWeeks(@PathVariable Long id, @RequestBody ActiveWeeksRequest req) {
        return service.updateActiveWeeks(id, req);
    }

    @PostMapping("/sections/{id}/student-invitations")
    public List<StudentInvitationResponse> inviteStudents(@PathVariable Long id, @Valid @RequestBody StudentInvitationRequest req) {
        return service.inviteStudents(id, req);
    }

    @GetMapping("/teams")
    public List<TeamSummaryResponse> getTeams(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String sectionName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) Long instructorId
    ) {
        return service.getTeams(sectionId, sectionName, teamName, instructorId);
    }

    @GetMapping("/teams/{id}")
    public TeamDetailResponse getTeam(@PathVariable Long id) {
        return service.getTeam(id);
    }

    @PostMapping("/teams")
    public TeamDetailResponse createTeam(@Valid @RequestBody TeamRequest req) {
        return service.createTeam(req);
    }

    @PutMapping("/teams/{id}")
    public TeamDetailResponse updateTeam(@PathVariable Long id, @Valid @RequestBody TeamRequest req) {
        return service.updateTeam(id, req);
    }

    @DeleteMapping("/teams/{id}")
    public void deleteTeam(@PathVariable Long id) {
        service.deleteTeam(id);
    }

    @DeleteMapping("/teams/{teamId}/students/{studentId}")
    public TeamDetailResponse removeStudentFromTeam(@PathVariable Long teamId, @PathVariable Long studentId) {
        return service.removeStudentFromTeam(teamId, studentId);
    }

    @DeleteMapping("/teams/{teamId}/instructors/{instructorId}")
    public TeamDetailResponse removeInstructorFromTeam(@PathVariable Long teamId, @PathVariable Long instructorId) {
        return service.removeInstructorFromTeam(teamId, instructorId);
    }

    @GetMapping("/rubric")
    public List<RubricCriterionResponse> getRubricCriteria(@RequestParam Long sectionId) {
        return service.getRubricCriteria(sectionId);
    }

    @PostMapping("/rubric")
    public RubricDetailResponse createRubricCriterion(@Valid @RequestBody RubricRequest req) {
        return service.createRubric(req);
    }

    @PutMapping("/rubric/{id}")
    public RubricDetailResponse updateRubricCriterion(@PathVariable Long id, @Valid @RequestBody RubricRequest req) {
        return service.updateRubric(id, req);
    }

    @GetMapping("/sections/{id}/rubric")
    public RubricDetailResponse getSectionRubric(@PathVariable Long id) {
        return service.getSectionRubric(id);
    }

    @GetMapping("/legacy/rubric-criteria")
    public List<RubricCriterionResponse> getAllRubricCriteria() {
        return service.getRubricCriteria();
    }
}
