package com.verify_x.controller;

import com.verify_x.dto.CandidateDocumentDto;
import com.verify_x.dto.CandidateDocumentRequest;
import com.verify_x.dto.CandidateProfileDto;
import com.verify_x.entity.CandidateDocument;
import com.verify_x.enums.DocumentType;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class CandidateDocumentController {

    private final CandidateDocumentService candidateDocumentService;

    // ==========================================================
    // Upload Document (Candidate)
    // ==========================================================

    @PostMapping(
            value="/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<String>> uploadDocuments(

            @ModelAttribute CandidateDocumentRequest request
    ) {

        candidateDocumentService.uploadDocuments(request);

        return ResponseEntity.ok(

                ApiResponse.success(
                        "Documents uploaded successfully.",
                        null
                )
        );
    }

    // ==========================================================
    // Re Upload
    // ==========================================================
    @PutMapping(
            value="/re-upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<String>> reUploadDocuments(

            @ModelAttribute CandidateDocumentRequest request
    ) {

        candidateDocumentService.reUploadDocuments(request);

        return ResponseEntity.ok(

                ApiResponse.success(
                        "Documents re-uploaded successfully.",
                        null
                )
        );
    }


    // ==========================================================
    // Logged In Candidate Documents
    // ==========================================================

    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/my-documents")
    public ResponseEntity<List<CandidateDocumentDto>> getMyDocuments() {

        return ResponseEntity.ok(
                candidateDocumentService.getMyDocuments());

    }

    // ==========================================================
    // HR/Admin View Candidate Documents
    // ==========================================================

//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    @GetMapping("/candidate/{candidateId}")
//    public ResponseEntity<ApiResponse<List<CandidateDocumentDto>>> getCandidateDocuments(
//
//            @PathVariable Long candidateId
//
//    ) {
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Documents fetched successfully.",
//                        (CandidateProfileDto) candidateDocumentService.getDocumentsByCandidateId(candidateId)
//                )
//        );
//    }
//@PreAuthorize("hasAnyRole('HR','ADMIN')")
//@GetMapping("/candidate/{candidateId}")
//public ResponseEntity<ApiResponse<List<CandidateDocumentDto>>> getCandidateDocuments(
//        @PathVariable Long candidateId) {
//
//    return ResponseEntity.ok(
//            ApiResponse.<List<CandidateDocumentDto>>builder()
//                    .success(true)
//                    .message("Documents fetched successfully.")
//                    .data(candidateDocumentService.getDocumentsByCandidateId(candidateId))
//                    .build()
//    );
//}

    // ==========================================================
    // View Document
    // ==========================================================

//    @PreAuthorize("hasAnyRole('CANDIDATE','HR','ADMIN')")
//    @GetMapping("/view/{documentId}")
//    public ResponseEntity<byte[]> viewDocument(
//            @PathVariable Long documentId
//    ) {
//
//        CandidateDocument document =
//                candidateDocumentService.getDocument(documentId);
//
//        return ResponseEntity.ok()
//                .header(
//                        HttpHeaders.CONTENT_DISPOSITION,
//                        "inline; filename=\"" + document.getFileName() + "\""
//                )
//                .contentType(
//                        MediaType.parseMediaType(document.getContentType())
//                )
//                .body(document.getDocumentData());
//    }


    @PreAuthorize("hasAnyRole('CANDIDATE','HR','ADMIN')")
    @GetMapping("/view/{documentId}")
    public ResponseEntity<byte[]> viewDocument(@PathVariable Long documentId) {
        CandidateDocument document = candidateDocumentService.getDocument(documentId);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(document.getContentType());
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + document.getFileName().replace("\"", "") + "\"")
                .contentType(mediaType)
                .body(document.getDocumentData());
    }

    // ==========================================================
    // Delete Document
    // ==========================================================

    @PreAuthorize("hasRole('CANDIDATE')")
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<String>> deleteDocument(

            @PathVariable Long documentId

    ) {

        candidateDocumentService.deleteDocument(documentId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Document deleted successfully.",
                        (CandidateProfileDto) null
                )
        );
    }

    // ==========================================================
    // Verify Document (HR/Admin)
    // ==========================================================

//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    @PutMapping("/verify/{documentId}")
//    public ResponseEntity<ApiResponse<String>> verifyDocument(
//
//            @PathVariable Long documentId
//
//    ) {
//
//        candidateDocumentService.verifyDocument(documentId);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Document verified successfully.",
//                        (CandidateProfileDto) null
//                )
//        );
//    }

    // ==========================================================
    // Reject Document (HR/Admin)
    // ==========================================================
//
//    @PreAuthorize("hasAnyRole('HR','ADMIN')")
//    @PutMapping("/reject/{documentId}")
//    public ResponseEntity<ApiResponse<String>> rejectDocument(
//
//            @PathVariable Long documentId,
//
//            @RequestParam String rejectionReason
//
//    ) {
//
//        candidateDocumentService.rejectDocument(
//                documentId,
//                rejectionReason
//        );
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "Document rejected successfully.",
//                        (CandidateProfileDto) null
//                )
//        );
//    }

//    @PutMapping("/verify-uan/{candidateId}")
//    public ResponseEntity<ApiResponse<String>> verifyUan(
//            @PathVariable Long candidateId) {
//
//        candidateDocumentService.verifyUan(candidateId);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(
//                        true,
//                        "UAN verified successfully.",
//                        null
//                )
//        );
//    }
}