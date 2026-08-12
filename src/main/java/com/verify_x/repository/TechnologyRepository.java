package com.verify_x.repository;

import com.verify_x.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {

    Optional<Technology> findByName(String name);

    List<Technology> findByNameContainingIgnoreCase(String name);
}