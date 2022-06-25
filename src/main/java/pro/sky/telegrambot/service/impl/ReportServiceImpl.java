package pro.sky.telegrambot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.repository.ReportsRepository;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.ReportService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Класс (сервис) ReportServiceImpl используется для работы с отчетами:
 * сохранение отчетов, сохранение фото для отчетов,
 * а так же поиск отчетов из базы данных.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    /**
     * @see ReportsRepository
     */
    private final ReportsRepository reportsRepository;

    /**
     * @see PicturesRepository
     */
    private final PicturesRepository picturesRepository;

    /**
     * @see UserRepository
     */
    private final UserRepository userRepository;

    /**
     * Конструктор, инжектим репозитории
     * @param reportsRepository  репозиторий отчетов
     * @param picturesRepository  репозиторий картинок
     * @param userRepository  репозиторий пользователей
     */
    public ReportServiceImpl(ReportsRepository reportsRepository, PicturesRepository picturesRepository, UserRepository userRepository) {
        this.reportsRepository = reportsRepository;
        this.picturesRepository = picturesRepository;
        this.userRepository = userRepository;
    }

    /**
     * Метод, сохраняющий новый отчет в репозитории
     * @param userId ID пользователя для поиска из репозитория
     * @param reportText текст отчета
     * @return возвращает сохраненный объект "report"
     */
    @Override
    public Report saveReport(Long userId, String reportText) {
        Report report = new Report();
        report.setReportText(reportText);
        report.setUser(userRepository.findById(userId).get());
        report.setReportDate();
        reportsRepository.save(report);

        return report;
    }

    /**
     * Метод, сохраняющий список фотографий для отчета
     * @param reportId ID отчета
     * @param files фотографии для сохранения
     * @return возвращает сохраненный объект "ReportPicture"
     * @throws IOException может вызвать исключение
     */
    @Override
    public Collection<ReportPicture> savePictures(Long reportId, List<MultipartFile> files) throws IOException {
        logger.info("==== Saving picture for report");
        Report report = reportsRepository.getById(reportId);
        ReportPicture picture = new ReportPicture();
        Collection<ReportPicture> pictures = new ArrayList<ReportPicture>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            picture.setReport(report);
            picture.setMediaType(file.getContentType());
            picture.setData(file.getBytes());
            picture.setFileSize(file.getSize());
            pictures.add(picture);
            picturesRepository.save(picture);
        }
        report.setPicturesOfReport(pictures);
        return pictures;
    }

    /**
     * Метод возвращает все отчеты по ID юзера
     * @param userId ID пользователя
     * @return возвращает коллекцию отчетов
     */
    @Override
    public Collection<Report> getReportsByParent(Long userId) {
        return reportsRepository.findAllByUser(userId);
    }
}
