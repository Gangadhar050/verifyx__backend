package com.verify_x.controller;

import com.verify_x.dto.HRCandidateDashboardDto;
import com.verify_x.dto.HRDashboardResponseDto;
import com.verify_x.services.HRDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HRDashboardController {

    private final HRDashboardService hrDashboardService;

    /**
     * Dashboard Report
     * GET : /api/hr/dashboard/report
     */
    @GetMapping("/report")
    public ResponseEntity<HRDashboardResponseDto> getDashboardReport() {

        return ResponseEntity.ok(
                hrDashboardService.getDashboardReport()
        );
    }

    /**
     * Recent Applications
     * GET : /api/hr/dashboard/candidates
     */
    @GetMapping("/candidates")
    public ResponseEntity<List<HRCandidateDashboardDto>> getAllCandidates() {

        return ResponseEntity.ok(
                hrDashboardService.getCandidates()
        );
    }

}
