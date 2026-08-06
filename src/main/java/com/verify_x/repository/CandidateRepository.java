package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.enums.CandidateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.verify_x.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByEmail(String email);

    Optional<Candidate> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    void deleteByEmail(String email);

    boolean existsByPanNumber(String panNumber);

    boolean existsByAadhaarNumber(String aadhaarNumber);

    List<Candidate> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username,
            String email
    );

    // Reports Dashboard
    long countByCandidateType(CandidateType candidateType);

    long countByApplicationStatus(ApplicationStatus applicationStatus);
}

    /*
     * HR Candidate Management: search + filter + pagination
     * (all filters optional; pass null to skip a filter)
     */
//    @Query("""
//        SELECT c FROM Candidate c
//        WHERE (:keyword IS NULL OR
//                LOWER(c.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
//                LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
//                c.phoneNumber LIKE CONCAT('%', :keyword, '%') OR
//                LOWER(c.appliedRole) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
//                CONCAT('VX-', c.id) LIKE CONCAT('%', :keyword, '%'))
//        AND (:candidateType IS NULL OR c.candidateType = :candidateType)
//        AND (:applicationStatus IS NULL OR c.applicationStatus = :applicationStatus)
//        """)
//    Page<Candidate> search(
//            @Param("keyword") String keyword,
//            @Param("candidateType") CandidateType candidateType,
//            @Param("applicationStatus") ApplicationStatus applicationStatus,
//            Pageable pageable
//    );
