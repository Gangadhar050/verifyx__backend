package com.verify_x.serviceImpl;

import com.verify_x.dto.*;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.entity.Education;
import com.verify_x.entity.Employment;
import com.verify_x.enums.*;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EducationRepository;
import com.verify_x.repository.EmploymentRepository;
import com.verify_x.services.CandidateManagementService;
import com.verify_x.services.EmailService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CandidateManagementServiceImpl
        implements CandidateManagementService {

    private final CandidateRepository candidateRepository;

    private final EmploymentRepository employmentRepository;

    private final CandidateDocumentRepository candidateDocumentRepository;

    private final EducationRepository educationRepository;

    private final EmailService emailService;


    // ============================================================
    // CANDIDATE SUMMARY
    // ============================================================

    private CandidateSummaryDto mapToSummary(
            Candidate candidate) {

        Employment employment =
                employmentRepository
                        .findByCandidate(candidate)
                        .orElse(null);

        return CandidateSummaryDto.builder()

                .id(candidate.getId())

                .fullName(candidate.getUsername())

                .email(candidate.getEmail())

                .phoneNumber(candidate.getPhoneNumber())

                .candidateType(candidate.getCandidateType())

                // Technical skills remain in Candidate entity
                .skills(candidate.getTechnicalSkills())

                .uanNumber(
                        employment != null
                                ? employment.getUanNumber()
                                : null
                )

                .uanVerified(
                        employment != null &&
                                Boolean.TRUE.equals(
                                        employment.getUanVerified()
                                )
                )

                .applicationStatus(
                        candidate.getApplicationStatus()
                )

                .build();
    }


    // ============================================================
    // DOCUMENT -> DTO
    // ============================================================

    private CandidateDocumentDto mapDocumentToDto(
            CandidateDocument document) {

        return CandidateDocumentDto.builder()

                .id(document.getId())

                .documentType(
                        document.getDocumentType()
                )

                .fileName(
                        document.getFileName()
                )

                .contentType(
                        document.getContentType()
                )

                .status(
                        document.getStatus()
                )

                .rejectionReason(
                        document.getRejectionReason()
                )

                .uploadedAt(
                        document.getUploadedAt()
                )

                .updatedAt(
                        document.getUpdatedAt()
                )

                .build();
    }


    // ============================================================
    // GET ALL CANDIDATES
    // ============================================================

    @Override
    public List<CandidateSummaryDto> getAllCandidates() {

        return candidateRepository
                .findAll()
                .stream()
                .map(this::mapToSummary)
                .toList();
    }


    // ============================================================
    // SEARCH CANDIDATES
    // ============================================================

    @Override
    public List<CandidateSummaryDto> searchCandidates(
            String keyword) {

        return candidateRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword,
                        keyword
                )
                .stream()
                .map(this::mapToSummary)
                .toList();
    }


    // ============================================================
    // GET CANDIDATE DETAILS
    // ============================================================

    @Override
    public CandidateDetailsDto getCandidateDetails(
            Long candidateId) {

        if (candidateId == null) {

            throw new BadRequestException(
                    "Candidate ID is required."
            );
        }

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId
                                )
                        );


        // ========================================================
        // EMPLOYMENT
        // ========================================================

        Employment employment =
                employmentRepository
                        .findByCandidate(candidate)
                        .orElse(null);


        // ========================================================
        // DOCUMENTS
        // ========================================================

        List<CandidateDocumentDto> documents =
                candidateDocumentRepository
                        .findByCandidate(candidate)
                        .stream()
                        .map(this::mapDocumentToDto)
                        .toList();


        // ========================================================
        // PROFILE
        // ========================================================

        CandidateProfileDto profile =
                CandidateProfileDto.builder()

                        .username(
                                candidate.getUsername()
                        )

                        .email(
                                candidate.getEmail()
                        )

                        .phoneNumber(
                                candidate.getPhoneNumber()
                        )

                        .address(
                                candidate.getAddress()
                        )

                        .panNumber(
                                candidate.getPanNumber()
                        )

                        .aadhaarNumber(
                                candidate.getAadhaarNumber()
                        )

                        .appliedRole(
                                candidate.getAppliedRole()
                        )

                        .candidateType(
                                candidate.getCandidateType()
                        )

                        .build();


        // ========================================================
        // EDUCATION
        // ========================================================

        Education educationEntity =
                educationRepository
                        .findByCandidate(candidate)
                        .orElse(null);

        EducationResponse education = null;

        if (educationEntity != null) {

            education =
                    EducationResponse.builder()

                            .id(
                                    educationEntity.getId()
                            )

                            // =================================================
                            // 10TH
                            // =================================================

                            .tenthSchoolName(
                                    educationEntity
                                            .getTenthSchoolName()
                            )

                            .tenthBoard(
                                    educationEntity
                                            .getTenthBoard()
                            )

                            .tenthSchoolLocation(
                                    educationEntity
                                            .getTenthSchoolLocation()
                            )

                            .tenthRollNumber(
                                    educationEntity
                                            .getTenthRollNumber()
                            )

                            .tenthPassingYear(
                                    educationEntity
                                            .getTenthPassingYear()
                            )

                            .tenthPercentage(
                                    educationEntity
                                            .getTenthPercentage()
                            )

                            .tenthMarksCardName(
                                    educationEntity
                                            .getTenthMarksCardName()
                            )


                            // =================================================
                            // 12TH
                            // =================================================

                            .twelfthInstitutionName(
                                    educationEntity
                                            .getTwelfthInstitutionName()
                            )

                            .twelfthLocation(
                                    educationEntity
                                            .getTwelfthLocation()
                            )

                            .twelfthBoardUniversity(
                                    educationEntity
                                            .getTwelfthBoardUniversity()
                            )

                            .twelfthRegistrationNumber(
                                    educationEntity
                                            .getTwelfthRegistrationNumber()
                            )

                            .twelfthPassingYear(
                                    educationEntity
                                            .getTwelfthPassingYear()
                            )

                            .twelfthPercentage(
                                    educationEntity
                                            .getTwelfthPercentage()
                            )

                            .twelfthMarksCardName(
                                    educationEntity
                                            .getTwelfthMarksCardName()
                            )


                            // =================================================
                            // DEGREE
                            // =================================================

                            .degreeName(
                                    educationEntity
                                            .getDegreeName()
                            )

                            .specialization(
                                    educationEntity
                                            .getSpecialization()
                            )

                            .collegeName(
                                    educationEntity
                                            .getCollegeName()
                            )

                            .universityName(
                                    educationEntity
                                            .getUniversityName()
                            )

                            .degreeLocation(
                                    educationEntity
                                            .getDegreeLocation()
                            )

                            .usnNumber(
                                    educationEntity
                                            .getUsnNumber()
                            )

                            .degreeStartYear(
                                    educationEntity
                                            .getDegreeStartYear()
                            )

                            .degreeEndYear(
                                    educationEntity
                                            .getDegreeEndYear()
                            )

                            .degreePercentage(
                                    educationEntity
                                            .getDegreePercentage()
                            )

                            .degreeCertificateName(
                                    educationEntity
                                            .getDegreeCertificateName()
                            )


                            // =================================================
                            // MASTER'S
                            // =================================================

                            .mastersDegree(
                                    educationEntity
                                            .getMastersDegree()
                            )

                            .mastersSpecialization(
                                    educationEntity
                                            .getMastersSpecialization()
                            )

                            .mastersCollege(
                                    educationEntity
                                            .getMastersCollege()
                            )

                            .mastersUniversity(
                                    educationEntity
                                            .getMastersUniversity()
                            )

                            .mastersLocation(
                                    educationEntity
                                            .getMastersLocation()
                            )

                            .mastersRegistrationNumber(
                                    educationEntity
                                            .getMastersRegistrationNumber()
                            )

                            .mastersStartYear(
                                    educationEntity
                                            .getMastersStartYear()
                            )

                            .mastersEndYear(
                                    educationEntity
                                            .getMastersEndYear()
                            )

                            .mastersPercentage(
                                    educationEntity
                                            .getMastersPercentage()
                            )

                            .mastersDegreeCertificateName(
                                    educationEntity
                                            .getMastersDegreeCertificateName()
                            )


                            // =================================================
                            // TECHNICAL SKILLS
                            // IMPORTANT:
                            // Skills remain in Candidate entity.
                            // =================================================

                            .technicalSkills(
                                    candidate.getTechnicalSkills()
                            )

                            .build();
        }


        // ========================================================
        // EMPLOYMENT DTO
        // ========================================================

        EmploymentDetailsDto employmentDto =
                employment == null
                        ? null
                        : EmploymentDetailsDto.builder()

                        .previousCompanyName(
                                employment.getPreviousCompanyName()
                        )

                        .previousDesignation(
                                employment.getPreviousDesignation()
                        )

                        .totalExperience(
                                employment.getTotalExperience()
                        )

                        .lastCTC(
                                employment.getLastCTC()
                        )

                        .lastWorkingDay(
                                employment.getLastWorkingDay()
                        )

                        .uanNumber(
                                employment.getUanNumber()
                        )

                        .employmentStatus(
                                employment.getEmploymentStatus()
                        )

                        .currentCompany(
                                employment.getCurrentCompany()
                        )

                        .currentDesignation(
                                employment.getCurrentDesignation()
                        )

                        .currentCTC(
                                employment.getCurrentCTC()
                        )

                        .noticePeriod(
                                employment.getNoticePeriod()
                        )

                        .offerLetterStatus(
                                employment.getOfferLetterStatus()
                        )

                        .offerCompanyName(
                                employment.getOfferCompanyName()
                        )

                        .offeredCTC(
                                employment.getOfferedCTC()
                        )

                        .joiningDate(
                                employment.getJoiningDate()
                        )

                        .offerReferenceNumber(
                                employment.getOfferReferenceNumber()
                        )

                        .build();


        // ========================================================
        // FINAL CANDIDATE DETAILS
        // ========================================================

        return CandidateDetailsDto.builder()

                .profile(profile)

                .education(education)

                .employment(employmentDto)

                .documents(documents)

                .applicationStatus(
                        candidate.getApplicationStatus()
                )

                .remarks(
                        candidate.getRemarks()
                )

                .uanVerified(
                        employment != null &&
                                Boolean.TRUE.equals(
                                        employment.getUanVerified()
                                )
                )

                .uanVerifiedBy(
                        employment != null
                                ? employment.getUanVerifiedBy()
                                : null
                )

                .build();
    }


    // ============================================================
    // CREATE CANDIDATE
    // ============================================================

    @Override
    public CandidateSummaryDto createCandidate(
            UserRegistrationDto dto) {

        if (dto == null) {

            throw new BadRequestException(
                    "Candidate details are required."
            );
        }

        if (candidateRepository.existsByEmail(
                dto.getEmail())) {

            throw new BadRequestException(
                    "Email already exists."
            );
        }

        if (candidateRepository.existsByPhoneNumber(
                dto.getPhoneNumber())) {

            throw new BadRequestException(
                    "Phone number already exists."
            );
        }

        Candidate candidate =
                Candidate.builder()

                        .username(
                                dto.getUsername()
                        )

                        .email(
                                dto.getEmail()
                        )

                        .phoneNumber(
                                dto.getPhoneNumber()
                        )

                        .password(
                                dto.getPassword()
                        )

                        .appliedRole(
                                dto.getAppliedRole()
                        )

                        .candidateType(
                                dto.getCandidateType()
                        )

                        .applicationStatus(
                                ApplicationStatus
                                        .PENDING_VERIFICATION
                        )

                        .build();

        candidateRepository.save(candidate);

        return mapToSummary(candidate);
    }


    // ============================================================
    // DELETE CANDIDATE
    // ============================================================

    @Override
    public void deleteCandidate(
            Long candidateId) {

        if (candidateId == null) {

            throw new BadRequestException(
                    "Candidate ID is required."
            );
        }

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId
                                )
                        );


        // Delete candidate documents

        candidateDocumentRepository
                .findByCandidate(candidate)
                .forEach(candidateDocumentRepository::delete);


        // Delete education

        educationRepository
                .findByCandidate(candidate)
                .ifPresent(educationRepository::delete);


        // Delete employment

        employmentRepository
                .findByCandidate(candidate)
                .ifPresent(employmentRepository::delete);


        // Finally delete candidate

        candidateRepository.delete(candidate);

        log.info(
                "Candidate {} deleted by HR.",
                candidateId
        );
    }


    // ============================================================
    // VERIFY UAN
    // ============================================================

    @Override
    public void verifyUan(
            Long candidateId,
            VerificationStatus status,
            String verifiedBy) {

        if (candidateId == null) {

            throw new BadRequestException(
                    "Candidate ID is required."
            );
        }

        if (status == null) {

            throw new BadRequestException(
                    "Verification status is required."
            );
        }

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId
                                )
                        );

        Employment employment =
                employmentRepository
                        .findByCandidate(candidate)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employment details not found."
                                )
                        );


        if (employment.getUanNumber() == null ||
                !employment.getUanNumber()
                        .matches("^\\d{12}$")) {

            throw new BadRequestException(
                    "UAN number must contain exactly 12 digits before verification."
            );
        }


        switch (status) {

            case VERIFIED -> {

                employment.setUanVerified(true);

                employment.setUanVerificationStatus(
                        VerificationStatus.VERIFIED
                );

                employment.setUanVerifiedBy(
                        verifiedBy != null
                                ? verifiedBy
                                : "HR"
                );

                employment.setUanVerifiedAt(
                        LocalDateTime.now()
                );
            }


            case REJECTED -> {

                employment.setUanVerified(false);

                employment.setUanVerificationStatus(
                        VerificationStatus.REJECTED
                );

                employment.setUanVerifiedBy(
                        verifiedBy != null
                                ? verifiedBy
                                : "HR"
                );

                employment.setUanVerifiedAt(
                        LocalDateTime.now()
                );
            }


            case PENDING -> {

                employment.setUanVerified(false);

                employment.setUanVerificationStatus(
                        VerificationStatus.PENDING
                );

                employment.setUanVerifiedBy(null);

                employment.setUanVerifiedAt(null);
            }
        }


        employmentRepository.save(employment);

        log.info(
                "UAN status for candidate {} set to {} by {}",
                candidateId,
                status,
                verifiedBy
        );
    }


    // ============================================================
    // UPDATE APPLICATION STATUS
    // ============================================================

    @Override
    public void updateApplicationStatus(
            Long candidateId,
            ApplicationStatusUpdateDto dto,
            String reviewedBy) {

        if (candidateId == null) {

            throw new BadRequestException(
                    "Candidate ID is required."
            );
        }

        if (dto == null ||
                dto.getStatus() == null) {

            throw new BadRequestException(
                    "Application status is required."
            );
        }

        Candidate candidate =
                candidateRepository
                        .findById(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Candidate",
                                        candidateId
                                )
                        );


        // ========================================================
        // APPROVAL VALIDATION
        // ========================================================

        if (dto.getStatus() ==
                ApplicationStatus.APPROVED) {


            // ----------------------------------------------------
            // UAN VALIDATION FOR EXPERIENCED
            // ----------------------------------------------------

            Employment employment =
                    employmentRepository
                            .findByCandidate(candidate)
                            .orElse(null);

            boolean uanOk =
                    candidate.getCandidateType()
                            != CandidateType.EXPERIENCED
                            ||
                            (
                                    employment != null &&
                                    Boolean.TRUE.equals(
                                            employment.getUanVerified()
                                    )
                            );


            // ----------------------------------------------------
            // DOCUMENTS
            // ----------------------------------------------------

            List<CandidateDocument> documents =
                    candidateDocumentRepository
                            .findByCandidate(candidate);


            Set<DocumentType> requiredDocuments;


            if (candidate.getCandidateType()
                    == CandidateType.FRESHER) {

                requiredDocuments =
                        Set.of(
                                DocumentType.RESUME,
                                DocumentType.PAN_CARD
                        );

            } else {

                requiredDocuments =
                        Set.of(
                                DocumentType.RESUME,
                                DocumentType.PAN_CARD,
                                DocumentType.OFFER_LETTER,
                                DocumentType.SALARY_SLIP,
                                DocumentType.RELIEVING_LETTER,
                                DocumentType.EXPERIENCE_LETTER,
                                DocumentType.UAN_PROOF
                        );
            }


            // ----------------------------------------------------
            // REJECTED DOCUMENT CHECK
            // ----------------------------------------------------

            boolean hasRejectedDocs =
                    documents.stream()
                            .anyMatch(doc ->
                                    doc.getStatus()
                                            == DocumentStatus.REJECTED
                            );


            // ----------------------------------------------------
            // PENDING DOCUMENT CHECK
            // ----------------------------------------------------

            boolean hasPendingDocs =
                    documents.stream()
                            .anyMatch(doc ->
                                    doc.getStatus()
                                            == DocumentStatus.PENDING
                            );


            // ----------------------------------------------------
            // MISSING DOCUMENT CHECK
            // ----------------------------------------------------

            boolean missingRequiredDocuments =
                    requiredDocuments
                            .stream()
                            .anyMatch(type ->
                                    documents.stream()
                                            .noneMatch(doc ->
                                                    doc.getDocumentType()
                                                            == type
                                            )
                            );


            // ----------------------------------------------------
            // VALIDATIONS
            // ----------------------------------------------------

            if (!uanOk) {

                throw new BadRequestException(
                        "Cannot approve. UAN must be verified."
                );
            }


            if (hasRejectedDocs) {

                throw new BadRequestException(
                        "Cannot approve. Candidate has rejected documents."
                );
            }


            if (hasPendingDocs) {

                throw new BadRequestException(
                        "Cannot approve. Some documents are still pending verification."
                );
            }


            if (missingRequiredDocuments) {

                throw new BadRequestException(
                        "Cannot approve. Required documents are missing."
                );
            }
        }


        // ========================================================
        // UPDATE STATUS
        // ========================================================

        candidate.setApplicationStatus(
                dto.getStatus()
        );

        candidate.setRemarks(
                dto.getRemarks()
        );

        candidateRepository.save(candidate);


        // ========================================================
        // EMAIL
        // ========================================================

        try {

            switch (dto.getStatus()) {

                case APPROVED ->

                        emailService.sendApplicationApprovedEmail(
                                candidate.getEmail(),
                                candidate.getUsername(),
                                dto.getRemarks()
                        );


                case REJECTED ->

                        emailService.sendApplicationRejectedEmail(
                                candidate.getEmail(),
                                candidate.getUsername(),
                                dto.getRemarks()
                        );


                case RE_UPLOAD_REQUIRED ->

                        emailService.sendReUploadRequestEmail(
                                candidate.getEmail(),
                                candidate.getUsername(),
                                dto.getRemarks()
                        );


                default -> {
                    // No email for intermediate statuses.
                }
            }

        } catch (Exception mailException) {

            log.warn(
                    "Application status updated for candidate {}, " +
                    "but email notification failed: {}",
                    candidateId,
                    mailException.getMessage()
            );
        }


        log.info(
                "Application status for candidate {} set to {} by {}",
                candidateId,
                dto.getStatus(),
                reviewedBy
        );
    }


    // ============================================================
    // UPDATE APPLICATION STATUS FROM DOCUMENTS
    // ============================================================

    private void updateApplicationStatus(
            Candidate candidate) {

        List<CandidateDocument> documents =
                candidateDocumentRepository
                        .findByCandidate(candidate);


        boolean rejected =
                documents.stream()
                        .anyMatch(d ->
                                d.getStatus()
                                        == DocumentStatus.REJECTED
                        );


        boolean pending =
                documents.stream()
                        .anyMatch(d ->
                                d.getStatus()
                                        == DocumentStatus.PENDING
                        );


        if (rejected) {

            candidate.setApplicationStatus(
                    ApplicationStatus.RE_UPLOAD_REQUIRED
            );

        } else if (pending) {

            candidate.setApplicationStatus(
                    ApplicationStatus.PENDING_VERIFICATION
            );

        } else {

            candidate.setApplicationStatus(
                    ApplicationStatus.DOCUMENTS_VERIFIED
            );
        }


        candidateRepository.save(candidate);
    }
}