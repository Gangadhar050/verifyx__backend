package com.verify_x.serviceImpl;

import com.verify_x.dto.*;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.entity.Education;
import com.verify_x.enums.*;
import com.verify_x.repository.EducationRepository;
import com.verify_x.entity.Employment;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.repository.CandidateDocumentRepository;
import com.verify_x.repository.CandidateRepository;
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
public class CandidateManagementServiceImpl implements CandidateManagementService {

    private final CandidateRepository candidateRepository;
    private final EmploymentRepository employmentRepository;
    private final CandidateDocumentRepository candidateDocumentRepository;
    private final EducationRepository educationRepository;
    private final EmailService emailService;

    private CandidateSummaryDto mapToSummary(Candidate candidate) {
        Employment employment = employmentRepository.findByCandidate(candidate).orElse(null);

        return CandidateSummaryDto.builder()
                .id(candidate.getId())
                .fullName(candidate.getUsername())
                .email(candidate.getEmail())
                .phoneNumber(candidate.getPhoneNumber())
                .candidateType(candidate.getCandidateType())
                .skills(candidate.getTechnicalSkills())
                .uanNumber(employment != null ? employment.getUanNumber() : null)
                .uanVerified(employment != null && Boolean.TRUE.equals(employment.getUanVerified()))
                .applicationStatus(candidate.getApplicationStatus())
                .build();
    }

    private CandidateDocumentDto mapDocumentToDto(CandidateDocument document) {
        return CandidateDocumentDto.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .status(document.getStatus())
                .rejectionReason(document.getRejectionReason())
                .uploadedAt(document.getUploadedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

@Override
public List<CandidateSummaryDto> getAllCandidates() {

    return candidateRepository.findAll()
            .stream()
            .map(this::mapToSummary)
            .toList();
}
    @Override
    public List<CandidateSummaryDto> searchCandidates(String keyword) {

        return candidateRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword,
                        keyword
                )
                .stream()
                .map(this::mapToSummary)
                .toList();
    }
    @Override
    public CandidateDetailsDto getCandidateDetails(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));

        Employment employment = employmentRepository.findByCandidate(candidate).orElse(null);

        List<CandidateDocumentDto> documents = candidateDocumentRepository
                .findByCandidate(candidate)
                .stream()
                .map(this::mapDocumentToDto)
                .toList();

        CandidateProfileDto profile = CandidateProfileDto.builder()
                .username(candidate.getUsername())
                .email(candidate.getEmail())
                .phoneNumber(candidate.getPhoneNumber())
                .address(candidate.getAddress())
                .panNumber(candidate.getPanNumber())
                .aadhaarNumber(candidate.getAadhaarNumber())
                .appliedRole(candidate.getAppliedRole())
                .candidateType(candidate.getCandidateType())
                .build();
        Education educationEntity = educationRepository
                .findByCandidate(candidate)
                .orElse(null);

        EducationResponse education = null;

        if (educationEntity != null) {

            education = EducationResponse.builder()
                    .id(educationEntity.getId())

                    .tenthSchoolName(educationEntity.getTenthSchoolName())
                    .tenthBoard(educationEntity.getTenthBoard())
                    .tenthSchoolLocation(educationEntity.getTenthSchoolLocation())
                    .tenthRollNumber(educationEntity.getTenthRollNumber())
                    .tenthPassingYear(educationEntity.getTenthPassingYear())
                    .tenthPercentage(educationEntity.getTenthPercentage())
                    .tenthMarksCardName(educationEntity.getTenthMarksCardName())

                    .twelfthInstitutionName(educationEntity.getTwelfthInstitutionName())
                    .twelfthBoardUniversity(educationEntity.getTwelfthBoardUniversity())
                    .twelfthStream(educationEntity.getTwelfthStream())
                    .twelfthRegistrationNumber(educationEntity.getTwelfthRegistrationNumber())
                    .twelfthPassingYear(educationEntity.getTwelfthPassingYear())
                    .twelfthPercentage(educationEntity.getTwelfthPercentage())
                    .twelfthMarksCardName(educationEntity.getTwelfthMarksCardName())

                    .degreeName(educationEntity.getDegreeName())
                    .specialization(educationEntity.getSpecialization())
                    .collegeName(educationEntity.getCollegeName())
                    .universityName(educationEntity.getUniversityName())
                    .usnNumber(educationEntity.getUsnNumber())
                    .degreeStartYear(educationEntity.getDegreeStartYear())
                    .degreeEndYear(educationEntity.getDegreeEndYear())
                    .degreePercentage(educationEntity.getDegreePercentage())
                    .backlogStatus(educationEntity.getBacklogStatus())
                    .degreeCertificateName(educationEntity.getDegreeCertificateName())

                    .mastersDegree(educationEntity.getMastersDegree())
                    .mastersSpecialization(educationEntity.getMastersSpecialization())
                    .mastersCollege(educationEntity.getMastersCollege())
                    .mastersUniversity(educationEntity.getMastersUniversity())
                    .mastersRegistrationNumber(educationEntity.getMastersRegistrationNumber())
                    .modeOfStudy(educationEntity.getModeOfStudy())
                    .mastersStartYear(educationEntity.getMastersStartYear())
                    .mastersEndYear(educationEntity.getMastersEndYear())
                    .mastersPercentage(educationEntity.getMastersPercentage())
                    .mastersMarksCardName(educationEntity.getMastersMarksCardName())
                    .mastersDegreeCertificateName(educationEntity.getMastersDegreeCertificateName())

                    .build();
        }
        EmploymentDetailsDto employmentDto = employment == null ? null : EmploymentDetailsDto.builder()
                .previousCompanyName(employment.getPreviousCompanyName())
                .previousDesignation(employment.getPreviousDesignation())
                .totalExperience(employment.getTotalExperience())
                .lastCTC(employment.getLastCTC())
                .lastWorkingDay(employment.getLastWorkingDay())
                .uanNumber(employment.getUanNumber())
                .employmentStatus(employment.getEmploymentStatus())
                .currentCompany(employment.getCurrentCompany())
                .currentDesignation(employment.getCurrentDesignation())
                .currentCTC(employment.getCurrentCTC())
                .noticePeriod(employment.getNoticePeriod())
                .offerLetterStatus(employment.getOfferLetterStatus())
                .offerCompanyName(employment.getOfferCompanyName())
                .offeredCTC(employment.getOfferedCTC())
                .joiningDate(employment.getJoiningDate())
                .offerReferenceNumber(employment.getOfferReferenceNumber())
                .build();

        return CandidateDetailsDto.builder()
                .profile(profile)
                .education(education)
                .employment(employmentDto)
                .documents(documents)
                .applicationStatus(candidate.getApplicationStatus())
                .remarks(candidate.getRemarks())
                .uanVerified(employment != null && Boolean.TRUE.equals(employment.getUanVerified()))
                .uanVerifiedBy(employment != null ? employment.getUanVerifiedBy() : null)
                .build();
    }

    @Override
    public CandidateSummaryDto createCandidate(UserRegistrationDto dto) {

        if (candidateRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists.");
        }

        if (candidateRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new BadRequestException("Phone number already exists.");
        }

        Candidate candidate = Candidate.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password(dto.getPassword())
                .appliedRole(dto.getAppliedRole())  
                .candidateType(dto.getCandidateType())
                .applicationStatus(ApplicationStatus.PENDING_VERIFICATION)
                .build();

        candidateRepository.save(candidate);

        return mapToSummary(candidate);
    }

    @Override
    public void deleteCandidate(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));

        candidateDocumentRepository.findByCandidate(candidate)
                .forEach(candidateDocumentRepository::delete);

        employmentRepository.findByCandidate(candidate)
                .ifPresent(employmentRepository::delete);

        candidateRepository.delete(candidate);
        log.info("Candidate {} deleted by HR.", candidateId);
    }

    @Override
    public void verifyUan(Long candidateId,VerificationStatus status, String verifiedBy) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));

        Employment employment = employmentRepository.findByCandidate(candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Employment details not found."));

        System.out.println("========== VERIFY UAN ==========");
        System.out.println("Employment ID : " + employment.getId());
        System.out.println("Candidate ID  : " + candidate.getId());
        System.out.println("UAN Number    : [" + employment.getUanNumber() + "]");
        System.out.println("================================");

        if (employment.getUanNumber() == null || !employment.getUanNumber().matches("^\\d{12}$")) {
            throw new BadRequestException("UAN number must contain exactly 12 digits before verification.");
        }
        switch (status) {

            case VERIFIED -> {

                employment.setUanVerified(true);

                employment.setUanVerificationStatus(VerificationStatus.VERIFIED);

                employment.setUanVerifiedBy("HR");

                employment.setUanVerifiedAt(LocalDateTime.now());
            }

            case REJECTED -> {

                employment.setUanVerified(false);

                employment.setUanVerificationStatus(VerificationStatus.REJECTED);

                employment.setUanVerifiedBy("HR");

                employment.setUanVerifiedAt(LocalDateTime.now());
            }

            case PENDING -> {

                employment.setUanVerified(false);

                employment.setUanVerificationStatus(VerificationStatus.PENDING);

                employment.setUanVerifiedBy(null);

                employment.setUanVerifiedAt(null);
            }
        }

        employmentRepository.save(employment);

        log.info("UAN verified for candidate {} by {}", candidateId, verifiedBy);
    }

    @Override
    public void updateApplicationStatus(Long candidateId, ApplicationStatusUpdateDto dto, String reviewedBy) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));

        if (dto.getStatus() == ApplicationStatus.APPROVED) {

            Employment employment =
                    employmentRepository.findByCandidate(candidate)
                            .orElse(null);

            boolean uanOk =
                    candidate.getCandidateType() != CandidateType.EXPERIENCED
                            || (employment != null &&
                            Boolean.TRUE.equals(employment.getUanVerified()));

            List<CandidateDocument> documents =
                    candidateDocumentRepository.findByCandidate(candidate);

            Set<DocumentType> requiredDocuments;

            if (candidate.getCandidateType() == CandidateType.FRESHER) {

                requiredDocuments = Set.of(
                        DocumentType.RESUME,
                        DocumentType.PAN_CARD
                );

            } else {


                //If documents are optional remove the below lines and set requiredDocuments to an empty set
                requiredDocuments = Set.of(
                        DocumentType.RESUME,
                        DocumentType.PAN_CARD,
                        DocumentType.OFFER_LETTER,
                        DocumentType.SALARY_SLIP,
                        DocumentType.RELIEVING_LETTER,
                        DocumentType.EXPERIENCE_LETTER,
                        DocumentType.UAN_PROOF

                );
            }
            boolean hasRejectedDocs = documents.stream()
                    .anyMatch(doc -> doc.getStatus() == DocumentStatus.REJECTED);

            boolean hasPendingDocs = documents.stream()
                    .anyMatch(doc -> doc.getStatus() == DocumentStatus.PENDING);

            boolean missingRequiredDocuments =

                    requiredDocuments.stream()

                            .anyMatch(type ->

                                    documents.stream()

                                            .noneMatch(doc -> doc.getDocumentType() == type));
            if (!uanOk) {
                throw new BadRequestException(
                        "Cannot approve. UAN must be verified.");
            }

            if (hasRejectedDocs) {
                throw new BadRequestException(
                        "Cannot approve. Candidate has rejected documents.");
            }
            if (hasPendingDocs) {
                throw new BadRequestException(
                        "Cannot approve. Some documents are still pending verification.");
            }
            if (missingRequiredDocuments) {
                throw new BadRequestException(
                        "Cannot approve. Required documents are missing.");
            }
        }

        candidate.setApplicationStatus(dto.getStatus());
        candidate.setRemarks(dto.getRemarks());
        candidateRepository.save(candidate);


        try {
            switch (dto.getStatus()) {
                case APPROVED -> emailService.sendApplicationApprovedEmail(
                        candidate.getEmail(),
                        candidate.getUsername(),
                        dto.getRemarks());
                case REJECTED -> emailService.sendApplicationRejectedEmail(
                        candidate.getEmail(),
                        candidate.getUsername(),
                        dto.getRemarks());
                case RE_UPLOAD_REQUIRED -> emailService.sendReUploadRequestEmail(
                        candidate.getEmail(),
                        candidate.getUsername(),
                        dto.getRemarks());
                default -> {
                    // No email for intermediate statuses.
                }
            }
        } catch (Exception mailException) {
            log.warn("Application status updated for candidate {}, but email notification failed: {}",
                    candidateId, mailException.getMessage());
        }
        log.info("Application status for candidate {} set to {} by {}", candidateId, dto.getStatus(), reviewedBy);
    }

    private void updateApplicationStatus(Candidate candidate) {

        List<CandidateDocument> documents =
                candidateDocumentRepository.findByCandidate(candidate);

        boolean rejected = documents.stream()
                .anyMatch(d -> d.getStatus() == DocumentStatus.REJECTED);

        boolean pending = documents.stream()
                .anyMatch(d -> d.getStatus() == DocumentStatus.PENDING);

        if (rejected) {
            candidate.setApplicationStatus(ApplicationStatus.RE_UPLOAD_REQUIRED);
        } else if (pending) {
            candidate.setApplicationStatus(ApplicationStatus.PENDING_VERIFICATION);
        } else {
            candidate.setApplicationStatus(ApplicationStatus.DOCUMENTS_VERIFIED);
        }

        candidateRepository.save(candidate);
    }
}
