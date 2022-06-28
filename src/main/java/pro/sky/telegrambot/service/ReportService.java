package pro.sky.telegrambot.service;

import org.springframework.web.multipart.MultipartFile;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.repository.ReportsRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface ReportService {

    Report saveReport(Long userId, String reportText);

    Collection<Report> getReportsByParent(Long userId);

    Collection<ReportPicture> savePictures(Long reportId, List<MultipartFile> files) throws IOException;




}
