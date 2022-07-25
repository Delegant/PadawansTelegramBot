package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import pro.sky.telegrambot.Dao.Impl.PictureNameDao;
import pro.sky.telegrambot.Dao.Impl.ReportDao;
import pro.sky.telegrambot.Dao.Impl.ReportPictureDao;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import pro.sky.telegrambot.exceptions.ReportNotFoundException;
import pro.sky.telegrambot.exceptions.UserIsNotAllowedToSendReportException;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.PictureName;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.PictureNameRepository;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.repository.ReportsRepository;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.PictureService;
import pro.sky.telegrambot.service.ReportService;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

    private final EntityManager entityManager;

    private final TelegramBot telegramBot;

    private final PictureNameRepository pictureNameRepository;

    /**
     * @see UserServiceImpl
     */
    private final UserServiceImpl repoService;

    private final ReportDao reportDao;

    private final PictureNameDao pictureNameDao;

    private final ReportPictureDao reportPictureDao;

    private final PictureService pictureService;

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
                             UserServiceImpl repoService,
                             PictureNameRepository pictureNameRepository,
                             TelegramBot telegramBot,
                             EntityManager entityManager,
                             ReportDao reportDao,
                             PictureService pictureService,
                             PictureNameDao pictureNameDao,
                             ReportPictureDao reportPictureDao) {
        this.reportsRepository = reportsRepository;
        this.picturesRepository = picturesRepository;
        this.userRepository = userRepository;
        this.repoService = repoService;
        this.pictureNameRepository = pictureNameRepository;
        this.telegramBot = telegramBot;
        this.entityManager = entityManager;
        this.reportDao = reportDao;
        this.pictureService = pictureService;
        this.pictureNameDao = pictureNameDao;
        this.reportPictureDao = reportPictureDao;
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
     * Если фотография пришла перед созданием отчета, то создается новый отчет.
     * Если после создания отчета прошло более 15 часов, то так же создается новый отчет с текстом "Empty".
     * Формат имен сохраняемых фотографий: telegramUserId_reportId_pictureNumber
     * @param reportId идентификатор отчета
     * @param files список файлов
     * @return возвращает коллекцию загруженных файлов
     * @throws IOException может выбрасывать исключение
     */
    @Transactional
    @Override
    public Collection<ReportPicture> savePictures(Long reportId, Long userId, List<MultipartFile> files) throws IOException {
        logger.info("==== Saving picture for report");

        Report report = reportDao.get(reportId).orElse(new Report());

        Collection<ReportPicture> pictures = new ArrayList<>();

        //Здесь происходит сохранение файлов картинок в репозиторий и локально
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            ReportPicture picture = new ReportPicture();
            Path filePath;
            int j = 0;

            //Метод do\while подбирает имя файла, не конфликтующее с уже имеющимися в репозитории и локально
            do {
                filePath = Path.of(picturesDirectory, userId+ "_" + reportId + "_" + i + j + "." + getExtensions(Objects.requireNonNull(file.getOriginalFilename())));
                j++;
            } while (new File(filePath.toString()).exists() || picturesRepository.findByFilePath(filePath.toString()).isPresent());

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
            pictureService.savePicture(picture);

            PictureName pictureName = new PictureName();
            pictureName.setFilename(filePath.getFileName().toString());
            pictureName.setReport(report);
            pictureNameRepository.save(pictureName);
        }

        logger.info("==== Picture saved successfully");
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

    /**
     * Сохраняет новый отчет
     * @param report отчет
     * @return новый сохраненный отчет
     */
    @Override
    public Report saveReport(Report report) {
        return reportsRepository.save(report);
    }

    /**
     * Метод получает фотографию из Телеграм и сохраняет её в репозиторий
     *
     * Сначала метод получает список PhotoSize, затем выбирает из него оригинал фотографии, скачивает её,
     * затем отправляет в метод prepareForSavingReportPictures() для образования MultiPartFile и пользуется методом
     * savePictures() для сохранения фотографий отчета
     *
     * @see ReportServiceImpl#savePictures(Long, Long, List)
     * @see ReportServiceImpl#getReportsByParent(Long)
     * @see ReportServiceImpl#prepareForSavingReportPictures(String)
     * @param userId Id пользователя
     * @param message объект сообщения из Телеграм
     * @throws IOException выбрасывает исключение IOException
     */
    @Override
    public void getPictureFromMessage(Long userId, Message message) throws IOException {

        User user = repoService.getUserByChatId(userId).orElse(new User(message.chat().id(),
                message.chat().lastName() + message.chat().firstName()));

        List<PhotoSize> photos = Arrays.asList(message.photo());

        String fileId = Objects.requireNonNull(photos.stream().max(Comparator.comparing(PhotoSize::fileSize))
                .orElse(null)).fileId();

        GetFile request = new GetFile(fileId);
        GetFileResponse getFileResponse = telegramBot.execute(request);

        com.pengrad.telegrambot.model.File file = getFileResponse.file();
        file.fileId();
        file.filePath();
        file.fileSize();

        String fullPath = telegramBot.getFullFilePath(file);

        String tempFileName = "/downloadedFromTelegramFile.jpg";
        String fullFilePath = picturesDirectory + tempFileName;

        try {
            FileUtils.copyURLToFile(
                    new URL(fullPath),
                    new File(picturesDirectory + tempFileName),
                    1000000,
                    100000000);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
        }
        savePictures(getLastReportId(userId), userId, prepareForSavingReportPictures(fullFilePath));

    }

    /**
     * Метод получает путь до локального файла и преобразует его в MultipartFile, затем упаковывает в список.
     *
     * @param filePath путь файла, сохраненного в локальную папку
     * @return список (из одного файла) MultipartFile
     * @throws IOException выбрасывает исключение IOException
     */
    public List<MultipartFile> prepareForSavingReportPictures(String filePath) throws IOException {

        List<MultipartFile> files = new ArrayList<>();
        File file = new File(filePath);

        FileItem fileItem = new DiskFileItem("file", "image/jpeg", false, filePath, 10000000, file );

        try (InputStream in = new FileInputStream(file); OutputStream out = fileItem.getOutputStream()) {
            in.transferTo(out);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid file: " + e, e);
        }

        CommonsMultipartFile multipartFile = new CommonsMultipartFile(fileItem);
        files.add(multipartFile);

        return files;
    }

    /**
     * Метод получает id пользователя и выдает id последнего отчета из репозитория
     * Если последний отчет был создан более 15 часов назад, то создается новый отчет
     * @param userId id пользователя
     * @return возвращает id последнего отчета пользователя
     */
    private Long getLastReportId(Long userId) {
        Long lastReportIndex = 0L;

        Long userIdInRepository = repoService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("User not found")).getId();
        Collection<Report> reports = reportsRepository.findAllByUserId(userIdInRepository);
        lastReportIndex = reports.stream().max(Comparator.comparing(Report::getReportDate)).get().getId();

        Report report = reportDao.get(lastReportIndex).orElse(new Report());

        LocalDateTime newReportCreationTime = report.getReportDate().plusHours(15);

        // Здесь выполняется проверка, прошло ли более 15 часов с момента написания последнего отчета, если прошло, то выполняется
        //создание нового отчета
        if (LocalDateTime.now().isAfter(newReportCreationTime)) {
            report = new Report();
            report.setReportText("Empty");
            report.setReportDate();
            report.setUser(userRepository.findUserByChatId(userId).get());
            report.setDefaultStatus();
            reportsRepository.save(report);

        }

        return lastReportIndex;
    }

    /**
     * Метод возвращает сообщение о необходимости добавить текст отчеча или фото отчета или подтверждение об успешном создании отчета
     * @param chatId id юзера
     * @return сообщение
     */
    @Override
    public String checkNewReportByUser(Long chatId) {
        Report report = reportDao.get(getLastReportId(chatId)).get();
        String result = "";
        if (report.getReportText().equals("Empty")) {
            result = "Напишите текст отчета";
        } else if (reportPictureDao.getReportPicturesByReport(report).size() == 0) {
            result = "Пришлите фотографию питомца";
        } else {
            result = "Спасибо, Ваш отчет принят.";
        }
        return result;
    }

    /**
     * Метод обновляет отчет -> добавляет новый текст, меняет статус отчета и добавляет время обновления отчета,
     * сохраняет отчет и возвращает текст обновленного отчета.
     * @param reportId id отчета
     * @param updatedText новый текст для отчета
     * @return итоговый текст отчета
     * @throws ReportNotFoundException если отчет не найден
     */
    @Override
    public String updateReport(Long reportId, String updatedText) {
        Report report = reportDao.get(reportId).orElseThrow(() -> new ReportNotFoundException("Отчет с таким id не найден!"));
        String reportText = "";
        if (report.getReportText().equals("Empty")) {
            reportText = updatedText;
        } else {
            reportText = report.getReportText() + " " + updatedText;
        }

        report.setReportText(reportText);
        report.setStatus(Report.Status.UPDATED);
        report.setReportUpdateDate();
        reportsRepository.save(report);
        return reportText;
    }

}