package edu.tcu.projectpulse.api;

import edu.tcu.projectpulse.domain.*;
import edu.tcu.projectpulse.dto.*;
import edu.tcu.projectpulse.service.ProjectPulseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectPulseController {

    private final ProjectPulseService service;

    @GetMapping("/users")
    public List<UserAccount> getUsers(@RequestParam(required = false) Role role) {
        return service.getUsers(role);
    }

    @GetMapping("/users/{id}")
    public UserAccount getUser(@PathVariable Long id) {
        return service.getUser(id);
    }

    @PostMapping("/users")
    public UserAccount createUser(@Valid @RequestBody UserAccountRequest req) {
        return service.createUser(req);
    }

    @PutMapping("/users/{id}")
    public UserAccount updateUser(@PathVariable Long id, @Valid @RequestBody UserAccountRequest req) {
        return service.updateUser(id, req);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
    }

    @GetMapping("/sections")
    public List<Section> getSections() {
        return service.getSections();
    }

    @GetMapping("/sections/{id}")
    public Section getSection(@PathVariable Long id) {
        return service.getSection(id);
    }

    @PostMapping("/sections")
    public Section createSection(@Valid @RequestBody SectionRequest req) {
        return service.createSection(req);
    }

    @PutMapping("/sections/{id}")
    public Section updateSection(@PathVariable Long id, @Valid @RequestBody SectionRequest req) {
        return service.updateSection(id, req);
    }

    @GetMapping("/teams")
    public List<Team> getTeams(@RequestParam(required = false) Long sectionId) {
        return service.getTeams(sectionId);
    }

    @GetMapping("/teams/{id}")
    public Team getTeam(@PathVariable Long id) {
        return service.getTeam(id);
    }

    @PostMapping("/teams")
    public Team createTeam(@Valid @RequestBody TeamRequest req) {
        return service.createTeam(req);
    }

    @PutMapping("/teams/{id}")
    public Team updateTeam(@PathVariable Long id, @Valid @RequestBody TeamRequest req) {
        return service.updateTeam(id, req);
    }

    @DeleteMapping("/teams/{id}")
    public void deleteTeam(@PathVariable Long id) {
        service.deleteTeam(id);
    }

    @PostMapping("/teams/{teamId}/students/{studentId}")
    public Team addStudentToTeam(@PathVariable Long teamId, @PathVariable Long studentId) {
        return service.addStudentToTeam(teamId, studentId);
    }

    @DeleteMapping("/teams/{teamId}/students/{studentId}")
    public Team removeStudentFromTeam(@PathVariable Long teamId, @PathVariable Long studentId) {
        return service.removeStudentFromTeam(teamId, studentId);
    }

    @PostMapping("/teams/{teamId}/instructors/{instructorId}")
    public Team addInstructorToTeam(@PathVariable Long teamId, @PathVariable Long instructorId) {
        return service.addInstructorToTeam(teamId, instructorId);
    }

    @DeleteMapping("/teams/{teamId}/instructors/{instructorId}")
    public Team removeInstructorFromTeam(@PathVariable Long teamId, @PathVariable Long instructorId) {
        return service.removeInstructorFromTeam(teamId, instructorId);
    }

    @GetMapping("/rubric")
    public List<RubricCriterion> getRubricCriteria() {
        return service.getRubricCriteria();
    }

    @PostMapping("/rubric")
    public RubricCriterion createRubricCriterion(@Valid @RequestBody RubricCriterionRequest req) {
        return service.createRubricCriterion(req);
    }

    @PutMapping("/rubric/{id}")
    public RubricCriterion updateRubricCriterion(@PathVariable Long id, @Valid @RequestBody RubricCriterionRequest req) {
        return service.updateRubricCriterion(id, req);
    }

    @GetMapping("/war")
    public List<WarActivity> getWarActivities() {
        return service.getWarActivities();
    }

    @PostMapping("/war")
    public WarActivity createWarActivity(@Valid @RequestBody WarActivityRequest req) {
        return service.createWarActivity(req);
    }

    @PutMapping("/war/{id}")
    public WarActivity updateWarActivity(@PathVariable Long id, @Valid @RequestBody WarActivityRequest req) {
        return service.updateWarActivity(id, req);
    }

    @DeleteMapping("/war/{id}")
    public void deleteWarActivity(@PathVariable Long id) {
        service.deleteWarActivity(id);
    }

    @PostMapping("/peer-evaluations")
    public PeerEvaluation submitPeerEvaluation(@Valid @RequestBody PeerEvaluationRequest req) {
        return service.submitPeerEvaluation(req);
    }

    @GetMapping("/students/{studentId}/peer-evaluation-report")
    public List<Map<String, Object>> getStudentPeerEvaluationReport(@PathVariable Long studentId) {
        return service.getStudentPeerEvaluationReport(studentId);
    }

    @PostMapping("/reports/peer-evaluation")
    public List<PeerEvaluation> generatePeerEvaluationReport(@Valid @RequestBody PeerEvaluationReportRequest req) {
        return service.generatePeerEvaluationReport(req);
    }

    @PostMapping("/reports/war")
    public List<WarActivity> generateWarReport(@Valid @RequestBody WarReportRequest req) {
        return service.generateWarReport(req);
    }
}
