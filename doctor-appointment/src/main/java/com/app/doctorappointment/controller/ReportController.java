package com.app.doctorappointment.controller;

import com.app.doctorappointment.dto.AnalyticsOverviewResponse;
import com.app.doctorappointment.dto.DailySummaryResponse;
import com.app.doctorappointment.service.ReportingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportingService reportingService;

    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/daily-summary")
    public DailySummaryResponse getDailySummary() {
        return reportingService.getDailySummary();
    }

    @GetMapping("/analytics")
    public AnalyticsOverviewResponse getAnalyticsOverview() {
        return reportingService.getAnalyticsOverview();
    }
}
