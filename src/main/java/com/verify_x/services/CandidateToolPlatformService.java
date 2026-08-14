package com.verify_x.services;

import com.verify_x.dto.ToolPlatformResponseDto;
import com.verify_x.dto.UpdateToolPlatformRequestDto;
import com.verify_x.enums.ToolPlatform;

import java.util.List;

public interface CandidateToolPlatformService {

    // =========================================================
    // GET ALL TOOLS & PLATFORMS
    // =========================================================

    ToolPlatformResponseDto getToolPlatforms();


    // =========================================================
    // UPDATE SELECTED TOOLS & PLATFORMS
    // =========================================================

    ToolPlatformResponseDto updateToolPlatforms(
            UpdateToolPlatformRequestDto request);


    // =========================================================
    // SEARCH TOOLS & PLATFORMS
    // =========================================================

    List<ToolPlatform> searchToolPlatforms(
            String keyword);


    // =========================================================
    // GET RECOMMENDED TOOLS & PLATFORMS
    // =========================================================

    ToolPlatformResponseDto getRecommendedToolPlatforms();


    // =========================================================
    // REMOVE SINGLE TOOL / PLATFORM
    // =========================================================

    ToolPlatformResponseDto removeToolPlatform(
            ToolPlatform toolPlatform);
}