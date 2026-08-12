package com.verify_x.controller;

import com.verify_x.dto.TechnologyResponse;
import com.verify_x.services.TechnologyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technologies")
public class TechnologyController {

    private final TechnologyService technologyService;

    public TechnologyController(TechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    // Get all technologies
    @GetMapping
    public List<TechnologyResponse> getAllTechnologies() {
        return technologyService.getAllTechnologies();
    }

    // Search technologies
    @GetMapping("/search")
    public List<TechnologyResponse> searchTechnologies(
            @RequestParam String keyword) {

        return technologyService.searchTechnologies(keyword);
    }
}