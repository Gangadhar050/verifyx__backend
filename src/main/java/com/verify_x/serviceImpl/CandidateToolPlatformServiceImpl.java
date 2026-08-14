package com.verify_x.serviceImpl;

import com.verify_x.dto.ToolPlatformDto;
import com.verify_x.dto.ToolPlatformResponseDto;
import com.verify_x.dto.UpdateToolPlatformRequestDto;
import com.verify_x.entity.Candidate;
import com.verify_x.enums.ToolPlatform;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.CandidateToolPlatformService;

import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Transactional
public class CandidateToolPlatformServiceImpl
        implements CandidateToolPlatformService {

    private final CandidateRepository candidateRepository;


    // =========================================================
    // GET ALL TOOLS & PLATFORMS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ToolPlatformResponseDto getToolPlatforms() {

        Candidate candidate = getLoggedInCandidate();

        List<ToolPlatform> selectedTools =
                candidate.getToolPlatforms() != null
                        ? candidate.getToolPlatforms()
                        : Collections.emptyList();

        List<ToolPlatform> recommendedTools =
                getRecommendations(candidate.getAppliedRole());

        List<ToolPlatformDto> tools =
                Arrays.stream(ToolPlatform.values())
                        .map(tool -> ToolPlatformDto.builder()
                                .value(tool.name())
                                .name(formatName(tool))
                                .selected(selectedTools.contains(tool))
                                .recommended(recommendedTools.contains(tool))
                                .build())
                        .toList();

        return ToolPlatformResponseDto.builder()
                .candidateId(candidate.getId())
                .appliedRole(candidate.getAppliedRole())
                .tools(tools)
                .build();
    }
    // =========================================================
    // UPDATE SELECTED TOOLS & PLATFORMS
    // =========================================================

    @Override
    public ToolPlatformResponseDto updateToolPlatforms(
            UpdateToolPlatformRequestDto request) {

        Candidate candidate = getLoggedInCandidate();

        List<ToolPlatform> tools =
                request.getToolPlatforms();

        if (tools == null) {
            tools = new ArrayList<>();
        }

        candidate.setToolPlatforms(
                new ArrayList<>(new LinkedHashSet<>(tools))
        );

        candidateRepository.save(candidate);

        return getToolPlatforms();
    }


    // =========================================================
    // SEARCH TOOLS & PLATFORMS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<ToolPlatform> searchToolPlatforms(
            String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return Arrays.asList(ToolPlatform.values());
        }

        String searchKeyword =
                keyword.trim().toLowerCase();

        return Arrays.stream(ToolPlatform.values())
                .filter(tool ->
                        tool.name()
                                .toLowerCase()
                                .contains(searchKeyword))
                .toList();
    }


    // =========================================================
    // GET RECOMMENDED TOOLS & PLATFORMS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ToolPlatformResponseDto getRecommendedToolPlatforms() {

        Candidate candidate = getLoggedInCandidate();

        List<ToolPlatform> recommended =
                getRecommendations(candidate.getAppliedRole());

        List<ToolPlatformDto> tools =
                recommended.stream()
                        .map(tool -> ToolPlatformDto.builder()
                                .value(tool.name())
                                .name(formatName(tool))
                                .selected(
                                        candidate.getToolPlatforms() != null
                                                && candidate.getToolPlatforms()
                                                .contains(tool)
                                )
                                .recommended(true)
                                .build())
                        .toList();

        return ToolPlatformResponseDto.builder()
                .candidateId(candidate.getId())
                .appliedRole(candidate.getAppliedRole())
                .tools(tools)
                .build();
    }


    // =========================================================
    // REMOVE SINGLE TOOL / PLATFORM
    // =========================================================

    @Override
    public ToolPlatformResponseDto removeToolPlatform(
            ToolPlatform toolPlatform) {

        Candidate candidate = getLoggedInCandidate();

        List<ToolPlatform> selectedTools =
                candidate.getToolPlatforms();

        if (selectedTools != null) {
            selectedTools.remove(toolPlatform);
        }

        candidateRepository.save(candidate);

        return getToolPlatforms();
    }


    // =========================================================
    // GET LOGGED-IN CANDIDATE
    // =========================================================

    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal()
                        instanceof UserPrincipal)) {

            throw new ResourceNotFoundException(
                    "Logged-in candidate not found."
            );
        }

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        Long userId = principal.getUserId();

        return candidateRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate not found."
                        )
                );
    }

    // =========================================================
    // ROLE-BASED RECOMMENDATIONS
    // =========================================================

    private List<ToolPlatform> getRecommendations(
            Candidate.AppliedRole appliedRole) {

        if (appliedRole == null) {
            return Collections.emptyList();
        }

        return switch (appliedRole) {

            case JAVA_DEVELOPER ->
                    List.of(
                            ToolPlatform.INTELLIJ_IDEA,
                            ToolPlatform.ECLIPSE,
                            ToolPlatform.MAVEN,
                            ToolPlatform.GRADLE,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.POSTMAN,
                            ToolPlatform.SWAGGER_OPENAPI,
                            ToolPlatform.DOCKER,
                            ToolPlatform.JENKINS,
                            ToolPlatform.GITHUB_ACTIONS
                    );

            case SPRING_BOOT_DEVELOPER ->
                    List.of(
                            ToolPlatform.INTELLIJ_IDEA,
                            ToolPlatform.ECLIPSE,
                            ToolPlatform.MAVEN,
                            ToolPlatform.GRADLE,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.POSTMAN,
                            ToolPlatform.SWAGGER_OPENAPI,
                            ToolPlatform.DOCKER,
                            ToolPlatform.JENKINS
                    );

            case FRONTEND_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.NPM,
                            ToolPlatform.YARN,
                            ToolPlatform.PNPM,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.FIGMA
                    );

            case REACT_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.NPM,
                            ToolPlatform.YARN,
                            ToolPlatform.PNPM,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.FIGMA
                    );

            case ANGULAR_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.NPM,
                            ToolPlatform.YARN,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.FIGMA
                    );

            case VUE_JS_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.NPM,
                            ToolPlatform.YARN,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.FIGMA
                    );

            case UI_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.FIGMA,
                            ToolPlatform.ADOBE_XD,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB
                    );

            case BACKEND_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.INTELLIJ_IDEA,
                            ToolPlatform.MAVEN,
                            ToolPlatform.GRADLE,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.POSTMAN,
                            ToolPlatform.SWAGGER_OPENAPI,
                            ToolPlatform.DOCKER
                    );

            case DOT_NET_DEVELOPER, C_SHARP_DEVELOPER ->
                    List.of(
                            ToolPlatform.VISUAL_STUDIO,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.POSTMAN,
                            ToolPlatform.SWAGGER_OPENAPI,
                            ToolPlatform.DOCKER,
                            ToolPlatform.JENKINS
                    );

            case PYTHON_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.JUPYTER_NOTEBOOK,
                            ToolPlatform.GOOGLE_COLAB,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.POSTMAN,
                            ToolPlatform.DOCKER
                    );

            case NODE_JS_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.NPM,
                            ToolPlatform.YARN,
                            ToolPlatform.PNPM,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.POSTMAN,
                            ToolPlatform.DOCKER
                    );

            case PHP_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.POSTMAN,
                            ToolPlatform.DOCKER
                    );

            case RUBY_ON_RAILS_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.POSTMAN,
                            ToolPlatform.DOCKER
                    );

            case GO_DEVELOPER ->
                    List.of(
                            ToolPlatform.VS_CODE,
                            ToolPlatform.GIT,
                            ToolPlatform.GITHUB,
                            ToolPlatform.POSTMAN,
                            ToolPlatform.DOCKER
                    );
        };
    }


    // =========================================================
    // DISPLAY NAME
    // =========================================================

    private String formatName(ToolPlatform tool) {

        return Arrays.stream(tool.name().split("_"))
                .map(word ->
                        word.substring(0, 1)
                                + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}