package com.verify_x.controller;

import com.verify_x.dto.ITRoleResponse;
import com.verify_x.dto.RecommendedSkillResponse;
import com.verify_x.services.ITRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class ITRoleController {

    private final ITRoleService roleService;

    public ITRoleController(ITRoleService roleService) {
        this.roleService = roleService;
    }

    // Get all IT roles
    @GetMapping
    public List<ITRoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    // Search IT roles
    @GetMapping("/search")
    public List<ITRoleResponse> searchRoles(
            @RequestParam String keyword) {

        return roleService.searchRoles(keyword);
    }

    // Get recommended skills for a role
    @GetMapping("/{roleId}/recommended-skills")
    public List<RecommendedSkillResponse> getRecommendedSkills(
            @PathVariable Long roleId) {

        return roleService.getRecommendedSkills(roleId);
    }
}