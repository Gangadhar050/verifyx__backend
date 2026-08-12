package com.verify_x.services;

import com.verify_x.dto.ITRoleResponse;
import com.verify_x.dto.RecommendedSkillResponse;
import com.verify_x.repository.ITRoleRepository;
import com.verify_x.repository.ITRoleTechnologyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ITRoleService {

    private final ITRoleRepository roleRepository;
    private final ITRoleTechnologyRepository roleTechnologyRepository;

    public ITRoleService(
            ITRoleRepository roleRepository,
            ITRoleTechnologyRepository roleTechnologyRepository) {

        this.roleRepository = roleRepository;
        this.roleTechnologyRepository = roleTechnologyRepository;
    }

    // Get all IT roles
    public List<ITRoleResponse> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(role -> new ITRoleResponse(
                        role.getId(),
                        role.getName()
                ))
                .toList();
    }

    // Search IT roles
    public List<ITRoleResponse> searchRoles(String keyword) {

        return roleRepository
                .findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(role -> new ITRoleResponse(
                        role.getId(),
                        role.getName()
                ))
                .toList();
    }

    // Get recommended technologies for a role
    public List<RecommendedSkillResponse> getRecommendedSkills(Long roleId) {

        return roleTechnologyRepository
                .findByRoleId(roleId)
                .stream()
                .map(roleTechnology -> new RecommendedSkillResponse(
                        roleTechnology.getTechnology().getId(),
                        roleTechnology.getTechnology().getName()
                ))
                .toList();
    }
}