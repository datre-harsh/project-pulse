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

    @PostMapping("/instructor-invitations")
    public List<InstructorInvitationResponse> inviteInstructors(@Valid @RequestBody InstructorInvitationRequest req) {
        return service.inviteInstructors(req);
    }

    @PostMapping("/students/register")
    public StudentRegistrationResponse registerStudent(@Valid @RequestBody StudentRegistrationRequest req) {
        return service.registerStudent(req);
    }

    @GetMapping("/students/profile")
    public ProfileUpdateResponse getStudentProfile() {
        // TODO: Get student ID from authentication context (currently hardcoded for demo)
        Long studentId = 1L; // This should come from the authenticated user
        UserAccount student = service.getUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can access their profile through this endpoint");
        }
        return new ProfileUpdateResponse(
                student.getId().toString(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                "Profile loaded successfully"
        );
    }

    @PutMapping("/students/profile")
    public ProfileUpdateResponse updateStudentProfile(@Valid @RequestBody ProfileUpdateRequest req) {
        // TODO: Get student ID from authentication context (currently hardcoded for demo)
        Long studentId = 1L; // This should come from the authenticated user
        return service.updateStudentProfile(studentId, req);
    }

    // Weekly Activity Report (WAR) Endpoints
    
    @GetMapping("/students/war")
    public List<WeeklyActivityResponse> getWeeklyActivities(
            @RequestParam(required = false) String weekId) {
        // TODO: Get student ID from authentication context (currently hardcoded for demo)
        String studentId = "1"; // This should come from the authenticated user
        return service.getWeeklyActivities(studentId, weekId);
    }

    @PostMapping("/students/war")
    public WeeklyActivityResponse createWeeklyActivity(@Valid @RequestBody WeeklyActivityRequest req) {
        // TODO: Get student ID from authentication context (currently hardcoded for demo)
        String studentId = "1"; // This should come from the authenticated user
        return service.createWeeklyActivity(studentId, req);
    }

    @PutMapping("/students/war/{activityId}")
    public WeeklyActivityResponse updateWeeklyActivity(
            @PathVariable String activityId, 
            @Valid @RequestBody WeeklyActivityRequest req) {
        // TODO: Get student ID from authentication context (currently hardcoded for demo)
        String studentId = "1"; // This should come from the authenticated user
        return service.updateWeeklyActivity(studentId, activityId, req);
    }

    @DeleteMapping("/students/war/{activityId}")
    public void deleteWeeklyActivity(@PathVariable String activityId) {
        // TODO: Get student ID from authentication context (currently hardcoded for demo)
        String studentId = "1"; // This should come from the authenticated user
        service.deleteWeeklyActivity(studentId, activityId);
    }

    // Peer Evaluation Endpoints
    
    @PostMapping("/students/peer-evaluation")
    public PeerEvaluationResponse submitPeerEvaluation(@Valid @RequestBody PeerEvaluationRequest req) {
        // TODO: Get student ID from authentication context (currently hardcoded for demo)
        Long evaluatorId = 1L; // This should come from the authenticated user
        return service.submitPeerEvaluation(evaluatorId, req);
    }

    @GetMapping("/students/peer-evaluations/report/{weekId}")
    public PeerEvaluationReportResponse getPeerEvaluationReport(@PathVariable String weekId) {
        // TODO: Get student ID from authentication context (currently hardcoded for demo)
        Long studentId = 1L; // This should come from the authenticated user
        return service.getPeerEvaluationReport(studentId, weekId);
    }

    // Instructor Registration Endpoints (UC-30)
    
    @PostMapping("/instructors/register")
    public InstructorRegistrationResponse registerInstructor(@Valid @RequestBody InstructorRegistrationRequest request) {
        return service.registerInstructor(request);
    }

    // Instructor Evaluation Endpoints (UC-31 Refactored)
    
    @GetMapping("/instructors/sections/{sectionId}/evaluations/{weekId}")
    public SectionEvaluationReportResponse getSectionEvaluationReport(@PathVariable Long sectionId, @PathVariable String weekId) {
        // TODO: Get instructor ID from authentication context (currently hardcoded for demo)
        Long instructorId = 2L; // This should come from the authenticated user
        return service.getSectionEvaluationReport(instructorId, sectionId, weekId);
    }

    // Team WAR Report Endpoints (UC-32)
    
    @GetMapping("/teams/{teamId}/war-report/{weekId}")
    public TeamWARReportResponse getTeamWARReport(@PathVariable Long teamId, @PathVariable String weekId) {
        return service.getTeamWARReport(teamId, weekId);
    }

    // Student Peer Evaluation Report Endpoints (UC-33)
    
    @GetMapping("/students/{studentId}/peer-evaluation-report")
    public List<WeeklyStudentReport> getStudentPeerEvaluationReport(
            @PathVariable Long studentId, 
            @RequestParam String startWeekId, 
            @RequestParam String endWeekId) {
        return service.getStudentPeerEvaluationReport(studentId, startWeekId, endWeekId);
    }

    // Student WAR Report Endpoints (UC-34)
    
    @GetMapping("/students/{studentId}/war-report")
    public List<WeeklyStudentWARReport> getStudentWARReport(
            @PathVariable Long studentId, 
            @RequestParam String startWeekId, 
            @RequestParam String endWeekId) {
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
