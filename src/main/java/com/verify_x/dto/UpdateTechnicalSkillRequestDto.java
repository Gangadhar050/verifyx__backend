package com.verify_x.dto;

import com.verify_x.enums.TechnicalSkill;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTechnicalSkillRequestDto {

    private List<TechnicalSkill> technicalSkills;
}