package com.verify_x.dto;

import com.verify_x.entity.Candidate;
import com.verify_x.enums.TechnicalSkill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HRCandidateDashboardDto {

    private Long id;

    private String username;

    private String email;

    private String candidateType;

    private String status;

    private List<TechnicalSkill> technicalSkills;

    private Candidate.AppliedRole appliedRole;
}