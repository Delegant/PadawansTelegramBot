package pro.sky.telegrambot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.telegrambot.model.ReportPicture;
import pro.sky.telegrambot.service.ReportService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Tag(name="Report Controller", description="API для сохранения отчетов, фотографий для отчетов и поиска отчетов по базе данных")
@RestController
@RequestMapping("/reports")
public class ReportController {

    Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(
            summary = "Сохранение нового отчета",
            description = "Сохраняет новый отчет, получая на вход два параметра: Id юзера и текст отчета"
    )
    @PostMapping("/newReport/{userId}&{reportText}")
    public void saveNewReport(@RequestParam @PathVariable("userId") @Parameter(description = "Идентификатор пользователя") Long userId,
                              @RequestParam @PathVariable("reportText") @Parameter(description = "Текст отчета") String reportText) {
        reportService.saveReport(userId, reportText);

    }

    @Operation(
            summary = "Сохранение фотографий",
            description = "Сохраняет список фотографий для отчета. Сначала нужно сохранить отчет!"
    )
    @PostMapping(value = "/{reportId}/pictures", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Collection<ReportPicture>> saveReportPictures(@PathVariable @Parameter(description = "Идентификатор отчета") Long reportId,
                                                                        @RequestParam @Parameter(description = "Список фотографий для отчета") List<MultipartFile> files) throws IOException {

        Collection<ReportPicture> savedCollection = new ArrayList<ReportPicture>();
        try {
            savedCollection = reportService.savePictures(reportId, files);
        } catch (Exception e) {
            logger.error("!!!! Cannot save pictures because of exception " + e);
        }
        return ResponseEntity.ok(savedCollection);
    }
}
