package pro.sky.telegrambot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;
import pro.sky.telegrambot.service.ReportService;
import pro.sky.telegrambot.service.impl.ReportServiceImpl;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Tag(name = "Report Controller", description = "API для сохранения отчетов, фотографий для отчетов и поиска отчетов по базе данных")
@RestController
@RequestMapping("/reports")
public class ReportController {

    Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    @Value("${path.to.reportpictures.folder}")
    private String picturesDirectory;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(
            summary = "Сохранение нового отчета",
            description = "Сохраняет новый отчет, получая на вход два параметра: Id юзера и текст отчета"
    )
    @PostMapping("/newReport/{userId}&{reportText}")
    public void saveNewReport(@PathVariable("userId") @Parameter(description = "Идентификатор пользователя") Long userId,
                              @PathVariable("reportText") @Parameter(description = "Текст отчета") String reportText) {

        reportService.saveReport(userId, reportText);
    }

    @Operation(
            summary = "Сохранения нового отчета",
            description = "Сохраняет новый отчет, принимая на вход объект - отчет"
    )
    @PostMapping("/new")
    public ResponseEntity<Report> saveReport(@RequestBody @Parameter(description = "Отчет пользователя", required = true) Report report) {

        reportService.saveReport(report.getUser().getId(), report.getReportText());
        return ResponseEntity.ok(report);

    }

    @Operation(
            summary = "Сохранение фотографий",
            description = "Сохраняет список фотографий для отчета. Сначала нужно сохранить отчет!"
    )
    @PostMapping(value = "/{reportId}/pictures", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> saveReportPictures(@PathVariable @Parameter(description = "Идентификатор отчета") Long reportId,
                                                     @RequestParam @Parameter(description = "Список фотографий для отчета") List<MultipartFile> files) throws IOException {
        try {
            reportService.savePictures(reportId, files);
        } catch (Exception e) {
            logger.error("!!!! Cannot save pictures because of exception " + e);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/pictures/{reportId}")
    public void getPicturesOfReport(@PathVariable @Parameter(description = "Идентификатор отчета") Long reportId, HttpServletResponse response)
            throws IOException {

        Collection<ReportPicture> pics = reportService.getReportPicturesByReportId(reportId);
        ReportPicture picture;
        ReportPicture[] picturesArray = pics.toArray(new ReportPicture[0]);

        for (int i = 0; i < pics.size(); i++) {
            picture = picturesArray[i];
            Path path = Path.of(picture.getFilePath());
            try (InputStream is = Files.newInputStream(path);
                 OutputStream os = response.getOutputStream()) {
                response.setStatus(200);
                response.setContentType(picture.getMediaType());
                response.setContentLength((int) picture.getFileSize());
                is.transferTo(os);
            }
        }
    }

    @GetMapping("/allPictures")
    public ResponseEntity<List<ReportPicture>> getAllPictures() {
        return ResponseEntity.ok(reportService.findAllPictures());
    }


    /**
     * Эндпоинт возвращает список имен файлов фотографий, принадлежащих отчету
     * @param reportId идентификатор отчета
     * @return список имен файлов фотографий
     * @see ReportServiceImpl#getReportPicturesNames
     */
    @Operation(
            summary = "Вывод списка имен файлов фотографий",
            description = "Выводит список имен файлов, котоsst были сохранены к отчету"
    )
    @GetMapping("/pictures/filenames/{reportId}")
    public ResponseEntity<List<String>> getPicturesFilenames(@PathVariable @Parameter(description = "ID отчета") Long reportId) {
        List<String> filenames = reportService.getReportPicturesNames(reportId);
        return ResponseEntity.ok(filenames);
    }


    /**
     * Эндпоинт, возвращающий фотографию по имени файла
     * @param filename имя файла
     * @param response ответ
     * @throws IOException ошибка ввода/вывода
     */
    @Operation(
            summary = "Вывод фотографии по имени файла",
            description = "Выводит фотографию из локального хранилища по имени файла"
    )
    @GetMapping(value = "/{filename}/report-picture-from-storage")
    public void downloadPicture(@PathVariable String filename, HttpServletResponse response) throws IOException{
        ReportPicture picture = reportService.getPictureFromStorageByFilename(filename);
        Path path = Path.of(picture.getFilePath());
        try(InputStream is = Files.newInputStream(path);
            OutputStream os = response.getOutputStream()) {
            response.setStatus(200);
            response.setContentType(picture.getMediaType());
            response.setContentLength((int) picture.getFileSize());
            is.transferTo(os);
        }
    }
}
