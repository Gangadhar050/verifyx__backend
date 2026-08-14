package com.verify_x.controller;

import com.verify_x.dto.TechnicalSkillResponseDto;
import com.verify_x.dto.UpdateTechnicalSkillRequestDto;
import com.verify_x.enums.TechnicalSkill;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateSkillService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate/technical-skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class CandidateSkillController {

    private final CandidateSkillService candidateSkillService;


    // =========================================================
    // GET ALL TECHNICAL SKILLS
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<TechnicalSkillResponseDto>>
    getTechnicalSkills() {

        return ResponseEntity.ok(
                ApiResponse.<TechnicalSkillResponseDto>builder()
                        .success(true)
                        .message("Technical skills fetched successfully.")
                        .data(candidateSkillService.getTechnicalSkills())
                        .build()
        );
    }


    // =========================================================
    // SEARCH TECHNICAL SKILLS
    // =========================================================

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TechnicalSkill>>>
    searchTechnicalSkills(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                ApiResponse.<List<TechnicalSkill>>builder()
                        .success(true)
                        .message("Technical skills search completed successfully.")
                        .data(
                                candidateSkillService
                                        .searchTechnicalSkills(keyword)
                        )
                        .build()
        );
    }


    // =========================================================
    // REMOVE SINGLE TECHNICAL SKILL
    // =========================================================

    @DeleteMapping("/{skill}")
    public ResponseEntity<ApiResponse<TechnicalSkillResponseDto>>
    removeTechnicalSkill(
            @PathVariable TechnicalSkill skill) {

        return ResponseEntity.ok(
                ApiResponse.<TechnicalSkillResponseDto>builder()
                        .success(true)
                        .message("Technical skill removed successfully.")
                        .data(
                                candidateSkillService
                                        .removeTechnicalSkill(skill)
                        )
                        .build()
        );
    }


    // =========================================================
    // UPDATE ALL SELECTED TECHNICAL SKILLS
    // =========================================================

    @PutMapping
    public ResponseEntity<ApiResponse<TechnicalSkillResponseDto>>
    updateTechnicalSkills(
            @Valid
            @RequestBody UpdateTechnicalSkillRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.<TechnicalSkillResponseDto>builder()
                        .success(true)
                        .message("Technical skills updated successfully.")
                        .data(
                                candidateSkillService
                                        .updateTechnicalSkills(request)
                        )
                        .build()
        );
    }


    // =========================================================
    // GET RECOMMENDED TECHNICAL SKILLS
    // =========================================================

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<TechnicalSkillResponseDto>>
    getRecommendedSkills() {

        return ResponseEntity.ok(
                ApiResponse.<TechnicalSkillResponseDto>builder()
                        .success(true)
                        .message("Recommended technical skills fetched successfully.")
                        .data(
                                candidateSkillService
                                        .getRecommendedSkills()
                        )
                        .build()
        );
    }
}	