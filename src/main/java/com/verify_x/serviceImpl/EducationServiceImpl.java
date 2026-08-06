package com.verify_x.serviceImpl;

import com.verify_x.dto.EducationRequest;
import com.verify_x.dto.EducationResponse;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Education;
import com.verify_x.enums.EducationDocumentType;
import com.verify_x.enums.TechnicalSkill;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EducationRepository;
import com.verify_x.services.EducationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;

    private final CandidateRepository candidateRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg"
    );
    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        assert principal != null;
        return candidateRepository.findById(principal.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));
    }

    private EducationResponse mapToResponse(Education education) {

        return EducationResponse.builder()

                .id(education.getId())

                .tenthSchoolName(education.getTenthSchoolName())
                .tenthBoard(education.getTenthBoard())
                .tenthSchoolLocation(education.getTenthSchoolLocation())
                .tenthRollNumber(education.getTenthRollNumber())
                .tenthPassingYear(education.getTenthPassingYear())
                .tenthPercentage(education.getTenthPercentage())
                .tenthMarksCardName(
                        education.getTenthMarksCardName())

                .twelfthInstitutionName(
                        education.getTwelfthInstitutionName())
                .twelfthBoardUniversity(
                        education.getTwelfthBoardUniversity())
                .twelfthStream(
                        education.getTwelfthStream())
                .twelfthRegistrationNumber(
                        education.getTwelfthRegistrationNumber())
                .twelfthPassingYear(
                        education.getTwelfthPassingYear())
                .twelfthPercentage(
                        education.getTwelfthPercentage())
                .twelfthMarksCardName(
                        education.getTwelfthMarksCardName())

                .degreeName(education.getDegreeName())
                .specialization(education.getSpecialization())
                .collegeName(education.getCollegeName())
                .universityName(education.getUniversityName())
                .usnNumber(education.getUsnNumber())
                .degreeStartYear(education.getDegreeStartYear())
                .degreeEndYear(education.getDegreeEndYear())
                .degreePercentage(education.getDegreePercentage())
                .backlogStatus(education.getBacklogStatus())
                .degreeCertificateName(
                        education.getDegreeCertificateName())

                .mastersDegree(education.getMastersDegree())
                .mastersSpecialization(
                        education.getMastersSpecialization())
                .mastersCollege(
                        education.getMastersCollege())
                .mastersUniversity(
                        education.getMastersUniversity())
                .mastersRegistrationNumber(
                        education.getMastersRegistrationNumber())
                .modeOfStudy(
                        education.getModeOfStudy())
                .mastersStartYear(
                        education.getMastersStartYear())
                .mastersEndYear(
                        education.getMastersEndYear())
                .mastersPercentage(
                        education.getMastersPercentage())
                .mastersMarksCardName(
                        education.getMastersMarksCardName())
                .mastersDegreeCertificateName(
                        education.getMastersDegreeCertificateName())
                .technicalSkills(
                        education.getCandidate().getTechnicalSkills()
                )
                .build();
    }
    private void validateFile(MultipartFile file) {

        // File is optional during update
        if (file == null || file.isEmpty()) {
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    "Maximum allowed file size is 5 MB."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {

            throw new BadRequestException(
                    "Only PDF, JPG, JPEG and PNG files are allowed."
            );
        }

        log.debug(
                "Validated education document: {}, Size: {} bytes",
                file.getOriginalFilename(),
                file.getSize()
        );
    }

    private void saveFile(
            MultipartFile file,
            Consumer<byte[]> dataSetter,
            Consumer<String> fileNameSetter,
            Consumer<String> contentTypeSetter) throws IOException {

        if (file == null || file.isEmpty()) {
            return;
        }

        validateFile(file);

        dataSetter.accept(file.getBytes());
        fileNameSetter.accept(file.getOriginalFilename());
        contentTypeSetter.accept(file.getContentType());
    }
    private void mapRequestToEntity(
            Education education,
            EducationRequest request) throws IOException {


        // 10th
        education.setTenthSchoolName(request.getTenthSchoolName());
        education.setTenthBoard(request.getTenthBoard());
        education.setTenthSchoolLocation(request.getTenthSchoolLocation());
        education.setTenthRollNumber(request.getTenthRollNumber());
        education.setTenthPassingYear(request.getTenthPassingYear());
        education.setTenthPercentage(request.getTenthPercentage());


        // 12th
        education.setTwelfthInstitutionName(request.getTwelfthInstitutionName());
        education.setTwelfthBoardUniversity(request.getTwelfthBoardUniversity());
        education.setTwelfthStream(request.getTwelfthStream());
        education.setTwelfthRegistrationNumber(request.getTwelfthRegistrationNumber());
        education.setTwelfthPassingYear(request.getTwelfthPassingYear());
        education.setTwelfthPercentage(request.getTwelfthPercentage());


        // Degree
        education.setDegreeName(request.getDegreeName());
        education.setSpecialization(request.getSpecialization());
        education.setCollegeName(request.getCollegeName());
        education.setUniversityName(request.getUniversityName());
        education.setUsnNumber(request.getUsnNumber());
        education.setDegreeStartYear(request.getDegreeStartYear());
        education.setDegreeEndYear(request.getDegreeEndYear());
        education.setDegreePercentage(request.getDegreePercentage());
        education.setBacklogStatus(request.getBacklogStatus());


        // Masters
        education.setMastersDegree(request.getMastersDegree());
        education.setMastersSpecialization(request.getMastersSpecialization());
        education.setMastersCollege(request.getMastersCollege());
        education.setMastersUniversity(request.getMastersUniversity());
        education.setMastersRegistrationNumber(request.getMastersRegistrationNumber());
        education.setModeOfStudy(request.getModeOfStudy());
        education.setMastersStartYear(request.getMastersStartYear());
        education.setMastersEndYear(request.getMastersEndYear());
        education.setMastersPercentage(request.getMastersPercentage());


        //Documents
        saveFile(
                request.getTenthMarksCard(),
                education::setTenthMarksCard,
                education::setTenthMarksCardName,
                education::setTenthMarksCardContentType
        );

        saveFile(
                request.getTwelfthMarksCard(),
                education::setTwelfthMarksCard,
                education::setTwelfthMarksCardName,
                education::setTwelfthMarksCardContentType
        );

        saveFile(
                request.getDegreeCertificate(),
                education::setDegreeCertificate,
                education::setDegreeCertificateName,
                education::setDegreeCertificateContentType
        );

        saveFile(
                request.getMastersMarksCard(),
                education::setMastersMarksCard,
                education::setMastersMarksCardName,
                education::setMastersMarksCardContentType
        );

        saveFile(
                request.getMastersDegreeCertificate(),
                education::setMastersDegreeCertificate,
                education::setMastersDegreeCertificateName,
                education::setMastersDegreeCertificateContentType
        );
        Candidate candidate = education.getCandidate();

        if (request.getTechnicalSkills() != null) {
            candidate.setTechnicalSkills(request.getTechnicalSkills());
            candidateRepository.save(candidate);
        }
    }

    @Override
    public EducationResponse saveEducation(EducationRequest request) {

        try {

            Candidate candidate = getLoggedInCandidate();

            if (educationRepository.existsByCandidate(candidate)) {
                throw new RuntimeException("Education details already exist.");
            }

            Education education = new Education();
            education.setCandidate(candidate);

            mapRequestToEntity(education, request);

            Education savedEducation = educationRepository.save(education);

            log.info("Education details saved for candidate {}", candidate.getId());

            return mapToResponse(savedEducation);

        } catch (IOException ex) {

            log.error("Failed to save education details.", ex);

            throw new RuntimeException("Unable to save education details.", ex);
        }
    }
    @Override
    public EducationResponse updateEducation(EducationRequest request) {

        try {

            Candidate candidate = getLoggedInCandidate();

            Education education = educationRepository
                    .findByCandidate(candidate)
                    .orElseThrow(() ->
                            new RuntimeException("Education details not found."));

            mapRequestToEntity(education, request);

            Education updatedEducation = educationRepository.save(education);

            log.info("Education details updated for candidate {}", candidate.getId());

            return mapToResponse(updatedEducation);

        } catch (IOException ex) {

            log.error("Failed to update education details.", ex);

            throw new RuntimeException("Unable to update education details.", ex);
        }
    }

    @Override
    public EducationResponse getMyEducation() {

        Candidate candidate = getLoggedInCandidate();

        Education education = educationRepository
                .findByCandidate(candidate)
                .orElseThrow(() ->
                        new RuntimeException("Education details not found."));

        return mapToResponse(education);
    }
    @Override
    public EducationResponse getEducationByCandidateId(Long candidateId) {

        Education education = educationRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Education details not found."));

        return mapToResponse(education);
    }
    @Override
    public Resource viewDocument(Long educationId,
                                 EducationDocumentType documentType) {

        Education education = educationRepository.findById(educationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Education details not found."));

        byte[] fileData;

        switch (documentType) {

            case TENTH_MARKS_CARD:
                fileData = education.getTenthMarksCard();
                break;

            case TWELFTH_MARKS_CARD:
                fileData = education.getTwelfthMarksCard();
                break;

            case DEGREE_CERTIFICATE:
                fileData = education.getDegreeCertificate();
                break;

            case MASTERS_MARKS_CARD:
                fileData = education.getMastersMarksCard();
                break;

            case MASTERS_DEGREE_CERTIFICATE:
                fileData = education.getMastersDegreeCertificate();
                break;

            default:
                throw new BadRequestException("Invalid document type.");
        }

        if (fileData == null || fileData.length == 0) {
            throw new ResourceNotFoundException("Document not found.");
        }

        return new ByteArrayResource(fileData);
    }

    @Override
    public void deleteEducation() {

        Candidate candidate = getLoggedInCandidate();

        Education education = educationRepository.findByCandidate(candidate)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Education details not found."));

        educationRepository.delete(education);

        log.info("Education deleted for candidate {}", candidate.getId());
    }

    @Override
    public List<TechnicalSkill> getMyTechnicalSkills() {

        Candidate candidate = getLoggedInCandidate();

        if (candidate.getTechnicalSkills() == null) {
            return List.of();
        }

        return new ArrayList<>(candidate.getTechnicalSkills());
    }
}