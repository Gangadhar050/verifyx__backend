package com.verify_x.services;

import com.verify_x.dto.TechnologyResponse;
import com.verify_x.repository.TechnologyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnologyService {

    private final TechnologyRepository technologyRepository;

    public TechnologyService(TechnologyRepository technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    // Get all technologies
    public List<TechnologyResponse> getAllTechnologies() {

        return technologyRepository.findAll()
                .stream()
                .map(technology -> new TechnologyResponse(
                        technology.getId(),
                        technology.getName()
                ))
                .toList();
    }

    // Search technologies
    public List<TechnologyResponse> searchTechnologies(String keyword) {

        return technologyRepository
                .findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(technology -> new TechnologyResponse(
                        technology.getId(),
                        technology.getName()
                ))
                .toList();
    }
}