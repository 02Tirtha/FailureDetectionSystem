package com.tirtha.sfd.controller;

import com.tirtha.sfd.dto.DashboardStatsDto;
import com.tirtha.sfd.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")

public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsDto getDashboardStats() {
        return dashboardService.getStats();
    }
}
