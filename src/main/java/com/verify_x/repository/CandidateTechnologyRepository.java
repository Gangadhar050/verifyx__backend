package com.verify_x.repository;

import com.verify_x.entity.CandidateTechnology;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CandidateTechnologyRepository
        extends JpaRepository<CandidateTechnology, Long> {

    @EntityGraph(attributePaths = {"technology"})
    List<CandidateTechnology> findByCandidateId(Long candidateId);

    boolean existsByCandidateIdAndTechnologyId(
            Long candidateId,
            Long technologyId);

    @Modifying
    @Query("""
        DELETE FROM CandidateTechnology ct
        WHERE ct.candidate.id = :candidateId
        AND ct.technology.id = :technologyId
    """)
    void deleteByCandidateIdAndTechnologyId(
            @Param("candidateId") Long candidateId,
            @Param("technologyId") Long technologyId);
}