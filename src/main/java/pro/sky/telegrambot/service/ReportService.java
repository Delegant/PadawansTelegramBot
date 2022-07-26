package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Message;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.telegrambot.model.PictureName;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.repository.ReportsRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public interface ReportService {

    Report saveReport(Long userId, String reportText);

    Report getReportById(Long reportId);

    Report saveReport(Report report);

    Boolean ifHasPhoto(Report report);

    Collection<Report> getReportsByParent(Long userId);

    Collection<ReportPicture> getReportPicturesByReportId(Long reportId);

    Collection<Report> getListOfReportsByUserName(String username);

    Collection<Report> getListOfReportsByUserId(Long userId);

    Collection<ReportPicture> savePictures(Long reportId, Long userId, List<MultipartFile> files) throws IOException;

    List<ReportPicture> findAllPictures();

    List<String> getReportPicturesNames(Long reportId);

    List<PictureName> getPictureNames(Report report);

    ReportPicture getPictureFromStorageByFilename(String filename);

    void getPictureFromMessage(Long userId, Message message) throws IOException;

    String checkNewReportByUser(Long chatId);

    String updateReport(Long reportId, String updatedText);

    Collection<Report> getUnreadReports();

    int getNumberOfPicturesByReport(Report report);


}
