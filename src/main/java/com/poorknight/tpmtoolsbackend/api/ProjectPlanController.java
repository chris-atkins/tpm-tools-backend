package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.APIProjectPlan;
import com.poorknight.tpmtoolsbackend.api.entity.APIProjectPlanPatch;
import com.poorknight.tpmtoolsbackend.api.entity.APIProjectPlanPatchRow;
import com.poorknight.tpmtoolsbackend.api.entity.APIProjectPlanPatchTask;
import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService.ProjectPlanNotFoundException;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlanPatchTemplate;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectConsistencyValidator.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/project-plans")
public class ProjectPlanController {

    private ProjectPlanService projectPlanService;

    @GetMapping("/{projectPlanId}")
    public APIProjectPlan getProjectPlan(@PathVariable Long projectPlanId) {
        try {
            ProjectPlan projectPlan = projectPlanService.getProjectPlan(projectPlanId);
            return APIProjectPlan.fromDomainObject(projectPlan);

        } catch(ProjectPlanNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Either the projectPlanId does not point to an existing project plan, or you do not have access to it.");
        }
    }

    @PatchMapping("/{projectPlanId}")
    public APIProjectPlan patchProjectPlan(@PathVariable Long projectPlanId, @RequestBody APIProjectPlanPatch patchRequest) {
        validatePatchRequestThrowingExceptions(projectPlanId, patchRequest);
        ProjectPlanPatchTemplate patchTemplate = patchRequest.toDomainObject();

        try {
            ProjectPlan projectPlan = projectPlanService.updateProjectPlan(patchTemplate);
            return APIProjectPlan.fromDomainObject(projectPlan);

        } catch(ProjectPlanUpdateConsistencyException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private void validatePatchRequestThrowingExceptions(Long projectPlanId, APIProjectPlanPatch patchRequest) {

        if (!Objects.equals(projectPlanId, patchRequest.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The project plan id must match the id in the url.");
        }

        for (APIProjectPlanPatchRow row : patchRequest.getRows()) {
            for (APIProjectPlanPatchTask task : row.getTasks()) {
                if (!Objects.equals(task.getRowId(), row.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tasks much have a rowId that matches the id of the row that contains the task in it's list of tasks." );
                }
            }
        }
    }

}
