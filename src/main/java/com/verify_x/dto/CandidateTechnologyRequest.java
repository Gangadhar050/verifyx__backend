package com.verify_x.dto;

import lombok.Data;

import java.util.List;

@Data
public class CandidateTechnologyRequest {

    private List<Long> technologyIds;
}