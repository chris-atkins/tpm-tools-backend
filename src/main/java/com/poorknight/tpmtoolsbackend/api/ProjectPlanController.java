package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.APIProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class ProjectPlanController {

    private ProjectPlanService projectPlanService;

    @GetMapping("/api/v1/project-plans/{projectPlanId}")
    public APIProjectPlan getProjectPlan(@PathVariable Long projectPlanId) {
        try {
            ProjectPlan projectPlan = projectPlanService.getProjectPlan(projectPlanId);
            return APIProjectPlan.fromDomainObject(projectPlan);
        } catch(ProjectPlanService.ProjectPlanNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Either the projectPlanId does not point to an existing project plan, or you do not have access to it.");
        }
    }
}
