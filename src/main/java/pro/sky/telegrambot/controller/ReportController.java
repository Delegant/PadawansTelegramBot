package pro.sky.telegrambot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.sky.telegrambot.service.ReportService;

@RestController
@RequestMapping("/reports")
public class ReportController {

    Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/newReport/{userId}&{reportText}")
    public void saveNewReport(@RequestParam @PathVariable("userId") Long userId,
                              @RequestParam @PathVariable("reportText") String reportText) {
        reportService.saveReport(userId, reportText);

    }
}
