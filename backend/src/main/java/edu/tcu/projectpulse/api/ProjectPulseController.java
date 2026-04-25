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
