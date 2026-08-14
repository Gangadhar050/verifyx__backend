package com.verify_x.dto;

import com.verify_x.entity.Candidate.AppliedRole;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolPlatformResponseDto {

    private Long candidateId;

    private AppliedRole appliedRole;

    private List<ToolPlatformDto> tools;
}