package com.verify_x.controller;

import com.verify_x.dto.CareerTrackResponse;
import com.verify_x.dto.CareerTrackTechnologyResponse;
import com.verify_x.entity.SkillDomain;
import com.verify_x.services.SkillService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    // Get all skill domains
    @GetMapping("/domains")
    public List<SkillDomain> getAllDomains() {
        return skillService.getAllDomains();
    }

    // Get career tracks for a domain
    @GetMapping("/domains/{domainId}/career-tracks")
    public List<CareerTrackResponse> getCareerTracks(
            @PathVariable Long domainId) {

        return skillService.getCareerTracksByDomain(domainId);
    }

    // Get technologies for a career track
    @GetMapping("/career-tracks/{careerTrackId}/technologies")
    public List<CareerTrackTechnologyResponse> getTechnologies(
            @PathVariable Long careerTrackId) {

        return skillService
                .getTechnologiesByCareerTrack(careerTrackId);
    }
}