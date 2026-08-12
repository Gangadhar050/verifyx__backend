package com.verify_x.services;

import com.verify_x.dto.TechnologyResponse;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateTechnology;
import com.verify_x.entity.Technology;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.CandidateTechnologyRepository;
import com.verify_x.repository.TechnologyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CandidateTechnologyService {

    private final CandidateRepository candidateRepository;
    private final CandidateTechnologyRepository candidateTechnologyRepository;
    private final TechnologyRepository technologyRepository;

    public CandidateTechnologyService(
            CandidateRepository candidateRepository,
            CandidateTechnologyRepository candidateTechnologyRepository,
            TechnologyRepository technologyRepository) {

        this.candidateRepository = candidateRepository;
        this.candidateTechnologyRepository = candidateTechnologyRepository;
        this.technologyRepository = technologyRepository;
    }

    // Get technologies selected by candidate
    public List<TechnologyResponse> getCandidateTechnologies(Long candidateId) {

        return candidateTechnologyRepository
                .findByCandidateId(candidateId)
                .stream()
                .map(candidateTechnology ->
                        new TechnologyResponse(
                                candidateTechnology.getTechnology().getId(),
                                candidateTechnology.getTechnology().getName()
                        ))
                .toList();
    }

    // Add technology to candidate
    public void addTechnology(
            Long candidateId,
            Long technologyId) {

        Candidate candidate = candidateRepository
                .findById(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        Technology technology = technologyRepository
                .findById(technologyId)
                .orElseThrow(() ->
                        new RuntimeException("Technology not found"));

        boolean exists =
                candidateTechnologyRepository
                        .existsByCandidateIdAndTechnologyId(
                                candidateId,
                                technologyId);

        if (!exists) {

            CandidateTechnology candidateTechnology =
                    new CandidateTechnology(
                            candidate,
                            technology);

            candidateTechnologyRepository.save(
                    candidateTechnology);
        }
    }

    // Remove technology from candidate
    public void removeTechnology(
            Long candidateId,
            Long technologyId) {

        candidateTechnologyRepository
                .deleteByCandidateIdAndTechnologyId(
                        candidateId,
                        technologyId);
    }
}