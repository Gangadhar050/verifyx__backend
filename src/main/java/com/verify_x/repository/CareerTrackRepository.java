package com.verify_x.repository;

import com.verify_x.entity.CareerTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerTrackRepository extends JpaRepository<CareerTrack, Long> {

    List<CareerTrack> findByDomainId(Long domainId);

    Optional<CareerTrack> findByNameAndDomainId(String name, Long domainId);
}