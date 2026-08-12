package com.verify_x.services;

import com.verify_x.dto.CareerTrackTechnologyResponse;
import com.verify_x.dto.CareerTrackResponse;
import com.verify_x.entity.SkillDomain;
import com.verify_x.repository.CareerTrackRepository;
import com.verify_x.repository.CareerTrackTechnologyRepository;
import com.verify_x.repository.SkillDomainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillService {

    private final SkillDomainRepository skillDomainRepository;
    private final CareerTrackRepository careerTrackRepository;
    private final CareerTrackTechnologyRepository careerTrackTechnologyRepository;

    public SkillService(
            SkillDomainRepository skillDomainRepository,
            CareerTrackRepository careerTrackRepository,
            CareerTrackTechnologyRepository careerTrackTechnologyRepository) {

        this.skillDomainRepository = skillDomainRepository;
        this.careerTrackRepository = careerTrackRepository;
        this.careerTrackTechnologyRepository =
                careerTrackTechnologyRepository;
    }

    // Get all skill domains
    public List<SkillDomain> getAllDomains() {
        return skillDomainRepository.findAll();
    }

    // Get career tracks for a domain
    public List<CareerTrackResponse> getCareerTracksByDomain(Long domainId) {

        return careerTrackRepository.findByDomainId(domainId)
                .stream()
                .map(track -> new CareerTrackResponse(
                        track.getId(),
                        track.getName()
                ))
                .toList();
    }

    // Get technologies for a career track
    @Transactional(readOnly = true)
    public List<CareerTrackTechnologyResponse> getTechnologiesByCareerTrack(
            Long careerTrackId) {

        return careerTrackTechnologyRepository
                .findByCareerTrackId(careerTrackId)
                .stream()
                .map(technology -> new CareerTrackTechnologyResponse(
                        technology.getId(),
                        technology.getTechnology().getName()
                ))
                .toList();
    }
}