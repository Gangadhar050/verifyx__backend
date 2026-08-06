package com.verify_x.repository;

import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.DocumentStatus;
import com.verify_x.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateDocumentRepository extends JpaRepository<CandidateDocument, Long>{
    // Get all documents of a candidate
    List<CandidateDocument> findByCandidate(Candidate candidate);

    // Find one document by candidate and type
    Optional<CandidateDocument> findByCandidateAndDocumentType(
            Candidate candidate,
            DocumentType documentType
    );

    // Find all documents using candidate id
    List<CandidateDocument> findByCandidateId(Long candidateId);

    // Find documents by verification status
    List<CandidateDocument> findByStatus(DocumentStatus status);

    // HR Search
    List<CandidateDocument> findByCandidateUsernameContainingIgnoreCaseOrCandidateEmailContainingIgnoreCase(
            String username,
            String email
    );

    // Check duplicate upload
    boolean existsByCandidateAndDocumentType(
            Candidate candidate,
            DocumentType documentType
    );

    // Delete candidate document
    void deleteByCandidateAndDocumentType(
            Candidate candidate,
            DocumentType documentType
    );

    // Reports Dashboard
    long countByStatus(DocumentStatus status);

    Long countByCandidate(Candidate candidate);
}
