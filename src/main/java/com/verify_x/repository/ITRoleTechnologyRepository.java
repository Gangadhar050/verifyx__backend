package com.verify_x.repository;

import com.verify_x.entity.ITRoleTechnology;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ITRoleTechnologyRepository
        extends JpaRepository<ITRoleTechnology, Long> {

    @EntityGraph(attributePaths = {"technology"})
    List<ITRoleTechnology> findByRoleId(Long roleId);
}