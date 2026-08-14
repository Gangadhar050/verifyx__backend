package com.verify_x.controller;

import com.verify_x.dto.ToolPlatformResponseDto;
import com.verify_x.dto.UpdateToolPlatformRequestDto;
import com.verify_x.enums.ToolPlatform;
import com.verify_x.payload.ApiResponse;
import com.verify_x.services.CandidateToolPlatformService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate/tool-platforms")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class CandidateToolPlatformController {

    private final CandidateToolPlatformService candidateToolPlatformService;


    // =========================================================
    // GET ALL TOOLS & PLATFORMS
    // =========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<ToolPlatformResponseDto>>
    getToolPlatforms() {

        return ResponseEntity.ok(
                ApiResponse.<ToolPlatformResponseDto>builder()
                        .success(true)
                        .message("Tools and platforms fetched successfully.")
                        .data(
                                candidateToolPlatformService
                                        .getToolPlatforms()
                        )
                        .build()
        );
    }


    // =========================================================
    // SEARCH TOOLS & PLATFORMS
    // =========================================================

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ToolPlatform>>>
    searchToolPlatforms(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                ApiResponse.<List<ToolPlatform>>builder()
                        .success(true)
                        .message("Tools and platforms search completed successfully.")
                        .data(
                                candidateToolPlatformService
                                        .searchToolPlatforms(keyword)
                        )
                        .build()
        );
    }


    // =========================================================
    // UPDATE SELECTED TOOLS & PLATFORMS
    // =========================================================

    @PutMapping
    public ResponseEntity<ApiResponse<ToolPlatformResponseDto>>
    updateToolPlatforms(
            @Valid
            @RequestBody UpdateToolPlatformRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.<ToolPlatformResponseDto>builder()
                        .success(true)
                        .message("Tools and platforms updated successfully.")
                        .data(
                                candidateToolPlatformService
                                        .updateToolPlatforms(request)
                        )
                        .build()
        );
    }


    // =========================================================
    // GET RECOMMENDED TOOLS & PLATFORMS
    // =========================================================

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<ToolPlatformResponseDto>>
    getRecommendedToolPlatforms() {

        return ResponseEntity.ok(
                ApiResponse.<ToolPlatformResponseDto>builder()
                        .success(true)
                        .message("Recommended tools and platforms fetched successfully.")
                        .data(
                                candidateToolPlatformService
                                        .getRecommendedToolPlatforms()
                        )
                        .build()
        );
    }


    // =========================================================
    // REMOVE SINGLE TOOL / PLATFORM
    // =========================================================

    @DeleteMapping("/{toolPlatform}")
    public ResponseEntity<ApiResponse<ToolPlatformResponseDto>>
    removeToolPlatform(
            @PathVariable ToolPlatform toolPlatform) {

        return ResponseEntity.ok(
                ApiResponse.<ToolPlatformResponseDto>builder()
                        .success(true)
                        .message("Tool/platform removed successfully.")
                        .data(
                                candidateToolPlatformService
                                        .removeToolPlatform(toolPlatform)
                        )
                        .build()
        );
    }
}