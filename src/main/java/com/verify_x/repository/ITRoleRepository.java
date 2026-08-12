package com.verify_x.repository;

import com.verify_x.entity.ITRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ITRoleRepository extends JpaRepository<ITRole, Long> {

    Optional<ITRole> findByName(String name);

    List<ITRole> findByNameContainingIgnoreCase(String name);
}