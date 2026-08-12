package com.verify_x.controller;

import com.verify_x.dto.EducationRequest;
import com.verify_x.dto.EducationResponse;
import com.verify_x.enums.EducationDocumentType;
import com.verify_x.enums.TechnicalSkill;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/education")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    /**
     * Save Education
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<EducationResponse>> saveEducation(
            @ModelAttribute EducationRequest request) {

        return ResponseEntity.ok(

                ApiResponse.<EducationResponse>builder()
                        .success(true)
                        .message("Education details saved successfully.")
                        .data(educationService.saveEducation(request))
                        .build()
        );
    }

    /**
     * Update Education
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<EducationResponse>> updateEducation(
            @ModelAttribute EducationRequest request) {

        return ResponseEntity.ok(

                ApiResponse.<EducationResponse>builder()
                        .success(true)
                        .message("Education details updated successfully.")
                        .data(educationService.updateEducation(request))
                        .build()
        );
    }

    /**
     * Logged-in Candidate Education
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<EducationResponse>> getMyEducation() {

        return ResponseEntity.ok(

                ApiResponse.<EducationResponse>builder()
                        .success(true)
                        .message("Education details fetched successfully.")
                        .data(educationService.getMyEducation())
                        .build()
        );
    }

    /**
     * HR/Admin
     */
    @GetMapping("hr/{candidateId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<EducationResponse>>
    getEducationByCandidateId(
            @PathVariable Long candidateId) {

        return ResponseEntity.ok(

                ApiResponse.<EducationResponse>builder()
                        .success(true)
                        .message("Education details fetched successfully.")
                        .data(educationService.getEducationByCandidateId(candidateId))
                        .build()
        );
    }

    /**
     * View Uploaded Document
     */
    @GetMapping("/hr/{educationId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Resource> viewDocument(

            @PathVariable Long educationId,

            @RequestParam EducationDocumentType documentType) {

        Resource resource =
                educationService.viewDocument(
                        educationId,
                        documentType);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Delete Education
     */
    @DeleteMapping("hr/delete")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ApiResponse<String>> deleteEducation() {

        educationService.deleteEducation();

        return ResponseEntity.ok(

                ApiResponse.<String>builder()
                        .success(true)
                        .message("Education deleted successfully.")
                        .data("Deleted")
                        .build()
        );
    }


    @GetMapping("/technical-skills")
    @PreAuthorize("hasRole('CANDIDATE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TechnicalSkill>>> getTechnicalSkills() {

        return ResponseEntity.ok(
                ApiResponse.<List<TechnicalSkill>>builder()
                        .success(true)
                        .message("Candidate technical skills fetched successfully.")
                        .data(educationService.getMyTechnicalSkills())
                        .build());
    }
    /**
     * Extract text from education document using AWS Textract
     */
    @PostMapping(
            value = "/extract-text",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<String>> extractEducationText(
            @RequestParam("file") MultipartFile file) {

        String extractedText =
                educationService.extractEducationText(file);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Education document text extracted successfully.")
                        .data(extractedText)
                        .build()
        );
    }
}