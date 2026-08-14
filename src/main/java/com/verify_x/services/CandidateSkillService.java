package com.verify_x.services;

import com.verify_x.dto.TechnicalSkillResponseDto;
import com.verify_x.dto.UpdateTechnicalSkillRequestDto;
import com.verify_x.enums.TechnicalSkill;

import java.util.List;

public interface CandidateSkillService {

    // =========================================================
    // GET ALL TECHNICAL SKILLS
    // =========================================================

    TechnicalSkillResponseDto getTechnicalSkills();


    // =========================================================
    // UPDATE ALL SELECTED TECHNICAL SKILLS
    // =========================================================

    TechnicalSkillResponseDto updateTechnicalSkills(
            UpdateTechnicalSkillRequestDto request);


    // =========================================================
    // SEARCH TECHNICAL SKILLS
    // =========================================================

    List<TechnicalSkill> searchTechnicalSkills(
            String keyword);


    // =========================================================
    // GET RECOMMENDED TECHNICAL SKILLS
    // =========================================================

    TechnicalSkillResponseDto getRecommendedSkills();


    // =========================================================
    // REMOVE SINGLE TECHNICAL SKILL
    // =========================================================

    TechnicalSkillResponseDto removeTechnicalSkill(
            TechnicalSkill skill);
}