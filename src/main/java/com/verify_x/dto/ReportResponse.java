package com.verify_x.dto;

import lombok.AllArgsConstructor;
import com.verify_x.entity.Candidate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    private Long candidateId;
    private String fullName;
    private String candidateType;
    private String status;
    private Candidate.AppliedRole appliedRole;
    private String email;
    private String phoneNumber;

}
