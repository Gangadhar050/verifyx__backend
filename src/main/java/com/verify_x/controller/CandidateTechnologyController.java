package com.verify_x.controller;

import com.verify_x.dto.TechnologyResponse;
import com.verify_x.services.CandidateTechnologyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate/technologies")
public class CandidateTechnologyController {

    private final CandidateTechnologyService candidateTechnologyService;

    public CandidateTechnologyController(
            CandidateTechnologyService candidateTechnologyService) {

        this.candidateTechnologyService =
                candidateTechnologyService;
    }

    // Get technologies selected by a candidate
    @GetMapping("/{candidateId}")
    public List<TechnologyResponse> getCandidateTechnologies(
            @PathVariable Long candidateId) {

        return candidateTechnologyService
                .getCandidateTechnologies(candidateId);
    }

    // Add technology to candidate
    @PostMapping("/{candidateId}/{technologyId}")
    public String addTechnology(
            @PathVariable Long candidateId,
            @PathVariable Long technologyId) {

        candidateTechnologyService.addTechnology(
                candidateId,
                technologyId);

        return "Technology added successfully";
    }

    // Remove technology from candidate
    @DeleteMapping("/{candidateId}/{technologyId}")
    public String removeTechnology(
            @PathVariable Long candidateId,
            @PathVariable Long technologyId) {

        candidateTechnologyService.removeTechnology(
                candidateId,
                technologyId);

        return "Technology removed successfully";
    }
}