package com.verify_x.services;

import com.verify_x.dto.EducationRequest;
import com.verify_x.dto.EducationResponse;
import com.verify_x.enums.EducationDocumentType;
import com.verify_x.enums.TechnicalSkill;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EducationService {

    // =========================================================
    // SAVE EDUCATION
    // =========================================================

    EducationResponse saveEducation(
            EducationRequest request
    );


    // =========================================================
    // UPDATE EDUCATION
    // =========================================================

    EducationResponse updateEducation(
            EducationRequest request
    );


    // =========================================================
    // GET MY EDUCATION
    // =========================================================

    EducationResponse getMyEducation();


    // =========================================================
    // GET EDUCATION BY CANDIDATE ID
    // =========================================================

    EducationResponse getEducationByCandidateId(
            Long candidateId
    );


    // =========================================================
    // VIEW EDUCATION DOCUMENT
    // =========================================================

    Resource viewDocument(
            Long educationId,
            EducationDocumentType documentType
    );


    // =========================================================
    // GET MY TECHNICAL SKILLS
    // =========================================================

    List<TechnicalSkill> getMyTechnicalSkills();


    // =========================================================
    // DELETE EDUCATION
    // =========================================================

    void deleteEducation();


    // =========================================================
    // AWS TEXTRACT - EXTRACT TEXT
    // =========================================================

    String extractEducationText(
            MultipartFile file
    );


    // =========================================================
    // AWS TEXTRACT - EXTRACT AND POPULATE EDUCATION
    // =========================================================

    EducationResponse extractAndPopulateEducation(
            EducationRequest request
    );
}