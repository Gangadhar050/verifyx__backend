package com.verify_x.repository;

import com.verify_x.entity.SkillDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillDomainRepository extends JpaRepository<SkillDomain, Long> {

    Optional<SkillDomain> findByName(String name);
}