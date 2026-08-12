package com.verify_x.repository;

import com.verify_x.entity.CareerTrackTechnology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerTrackTechnologyRepository
        extends JpaRepository<CareerTrackTechnology, Long> {

    List<CareerTrackTechnology> findByCareerTrackId(Long careerTrackId);

    boolean existsByCareerTrackIdAndTechnologyId(
            Long careerTrackId,
            Long technologyId
    );
}