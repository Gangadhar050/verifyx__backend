package com.verify_x.services;

import com.verify_x.dto.EducationRequest;
import com.verify_x.dto.EducationResponse;
import com.verify_x.enums.EducationDocumentType;
import com.verify_x.enums.TechnicalSkill;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EducationService {

    EducationResponse saveEducation(EducationRequest request);

    EducationResponse updateEducation(EducationRequest request);

    EducationResponse getMyEducation();

    EducationResponse getEducationByCandidateId(Long candidateId);

    Resource viewDocument(
            Long educationId,
            EducationDocumentType documentType);

    List<TechnicalSkill> getMyTechnicalSkills();

    void deleteEducation();

    /**
     * Test AWS Textract extraction.
     */
    String extractEducationText(MultipartFile file);
}