package pro.sky.telegrambot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.telegrambot.exceptions.UserIsNotAllowedToSendReportException;
import pro.sky.telegrambot.model.PictureName;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.PictureNameRepository;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.repository.ReportsRepository;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.ReportService;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

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

    private final PictureNameRepository pictureNameRepository;

    /**
     * @see UserRepoService
     */
    private final UserRepoService repoService;

    @Value("${path.to.reportpictures.folder}")
    private String picturesDirectory;

    /**
     * Конструктор, инжектим репозитории
     * @param reportsRepository  репозиторий отчетов
     * @param picturesRepository  репозиторий картинок
     * @param userRepository  репозиторий пользователей
     * @param repoService сервис репозитория пользователей
     */
    public ReportServiceImpl(ReportsRepository reportsRepository,
                             PicturesRepository picturesRepository,
                             UserRepository userRepository,
                             UserRepoService repoService,
                             PictureNameRepository pictureNameRepository) {
        this.reportsRepository = reportsRepository;
        this.picturesRepository = picturesRepository;
        this.userRepository = userRepository;
        this.repoService = repoService;
        this.pictureNameRepository = pictureNameRepository;
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
        User user = repoService.getUserByChatId(userId).
                orElseThrow(() -> new UserIsNotAllowedToSendReportException("User Is not a Parent and cannot send reports!"));
        report.setReportText(reportText);
        report.setUser(user);
        report.setReportDate();
        reportsRepository.save(report);

        logger.info("==== New Report Saved from user {} with message {}", user, reportText);
        return report;
    }

    @Override
    public Collection<Report> getListOfReportsByUserName(String username) {
        return reportsRepository.findAllByUserId(userRepository.findByName(username).get().getId());
    }

    @Override
    public Collection<Report> getListOfReportsByUserId(Long userId) {
        return reportsRepository.findAllByUserId(userId);
    }

    /**
     * Метод, возвращающий расширение файла
     * @param fileName имя файла
     * @return строка с расширением файла
     */
    private @NotNull String getExtensions(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Метод, сохраняющий список фотографий для отчета. Может принимать несколько файлов.
     * Фотографии сохраняются в Базу Данных и локально.
     * Формат имен сохраняемых фотографий: telegramUserId_reportId_pictureNumber
     * @param reportId идентификатор отчета
     * @param files список файлов
     * @return возвращает коллекцию загруженных файлов
     * @throws IOException может выбрасывать исключение
     */
    @Override
    public Collection<ReportPicture> savePictures(Long reportId, List<MultipartFile> files) throws IOException {
        logger.info("==== Saving picture for report");
        Report report = reportsRepository.getById(reportId);

        Long userId = report.getUser().getChatId();
        Collection<ReportPicture> pictures = new ArrayList<ReportPicture>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            ReportPicture picture = new ReportPicture();

            Path filePath = Path.of(picturesDirectory, userId+ "_" + reportId + "_" + i + "." + getExtensions(Objects.requireNonNull(file.getOriginalFilename())));
            String checkFileName = picturesDirectory + "/" + userId+ "_" + reportId + "_" + i + "." + getExtensions(Objects.requireNonNull(file.getOriginalFilename()));

            if (new File(checkFileName).exists()) {
                filePath = Path.of(picturesDirectory, userId + "_" + reportId + "_" + i + 1 + "." + getExtensions(Objects.requireNonNull(file.getOriginalFilename())));
            }
            Files.createDirectories(filePath.getParent());
            Files.deleteIfExists(filePath);

            try (InputStream is = file.getInputStream();
                 OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
                 BufferedInputStream bis = new BufferedInputStream(is, 2048);
                 BufferedOutputStream bos = new BufferedOutputStream(os,2048)) {

                bis.transferTo(bos);

            }

            picture.setReport(report);
            picture.setMediaType(file.getContentType());
            picture.setData(file.getBytes());
            picture.setFileSize(file.getSize());
            picture.setFilePath(filePath.toString());
            pictures.add(picture);
            picturesRepository.save(picture);

            PictureName pictureName = new PictureName();
            pictureName.setFilename(filePath.getFileName().toString());
            pictureName.setReport(report);
            pictureNameRepository.save(pictureName);
        }
        List<PictureName> picturesNames = new ArrayList<>(pictureNameRepository.findAllByReportId(reportId));
        report.setPictureNames(picturesNames);
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
        return reportsRepository.findAllByUserId(userId);
    }

    /**
     *  Метод возвращает все картинки по id отчета
     * @param reportId id отчета
     * @return коллекция картинок из Базы Данных
     */
    @Override
    public Collection<ReportPicture> getReportPicturesByReportId(Long reportId) {
        Report report = reportsRepository.getById(reportId);
        return picturesRepository.findAllByReport(report);
    }

    @Override
    public List<ReportPicture> findAllPictures() {

        PageRequest pageRequest = PageRequest.of(0, 10);
        return picturesRepository.findAll(pageRequest).getContent();
    }

    /**
     * Метод возвращает список названий файлов с фотографиями, принадлежащими к отчету.
     * @param reportId Идентификатор отчета
     * @return список имен файлов фотографий
     */
    @Override
    public List<String> getReportPicturesNames(Long reportId) {
        Collection<PictureName> pictureNames = pictureNameRepository.findAllByReportId(reportId);
        return pictureNames.stream().map(PictureName::getFilename).collect(Collectors.toList());
    }

    /**
     * Метод, возвращающий фотографию по названию файла
     * @param filename имя файла
     * @return фотография
     */
    @Override
    public ReportPicture getPictureFromStorageByFilename(String filename) {
        return Optional.of(picturesRepository.findByFilePathEndingWith(filename)).orElseThrow();
    }



}





