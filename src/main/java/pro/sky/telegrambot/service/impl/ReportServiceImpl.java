package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
import pro.sky.telegrambot.model.*;
import pro.sky.telegrambot.repository.PictureNameRepository;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.repository.ReportsRepository;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.PictureService;
import pro.sky.telegrambot.service.ReportService;
import pro.sky.telegrambot.service.TrialPeriodService;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

/**
 * Класс (сервис) ReportServiceImpl используется для работы с отчетами:
 * сохранение отчетов, сохранение фото для отчетов,
 * а так же поиск отчетов из базы данных.
 */
@EnableScheduling
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

    @Autowired
    private TrialPeriodService trialPeriodService;

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
    public Report saveReportFromBot(Long userId, String reportText) {
        updateReport(userId, reportText);
        Report report = null;
        if (reportDao.get(getLastReportId(userId)).isPresent()) {

            report = reportDao.get(getLastReportId(userId)).get();
        }
        return report;
    }

    @Override
    public Report getReportById(Long reportId) {
        return reportDao.get(reportId)
                .orElseThrow(() -> new ReportNotFoundException("!!!! Report not found with id = " + reportId));
    }

    @Override
    public Boolean ifHasPhoto(Report report) {
        return picturesRepository.findAllByReportId(report.getId()).size() != 0;
    }

    @Override
    public Collection<Report> getListOfReportsByUserName(String username) {
        return reportsRepository.findAllByUserId(userRepository.findByName(username).get().getId());
    }

    @Override
    public Collection<Report> getListOfReportsByUserId(Long chatId) {
        return reportsRepository.findAllByUserChatId(chatId);
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
        Report report = null;
        if (reportDao.get(getLastReportId(userId)).isPresent()) {
            report = reportDao.get(getLastReportId(userId)).get();
        }

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

    @Override
    public List<PictureName> getPictureNames(Report report) {
        return new ArrayList<>(pictureNameRepository.findAllByReportId(report.getId()));
    }

    /**
     * Метод, возвращающий фотографию по названию файла
     * @param filename имя файла
     * @return фотография
     */
    @Override
    public ReportPicture getPictureFromStorageByFilename(String filename) {
        return Optional.of(picturesRepository.findByFilePathContains(filename)).orElseThrow();
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
        boolean updatingReport = userRepository.findUserByChatId(userId).get().getTemp() != null;
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
        if (message.caption() != null) {
            Report report = createDefaultReport(userId);
            report.setReportText(message.caption());
            reportsRepository.save(report);

            savePictures(report.getId(), userId, prepareForSavingReportPictures(fullFilePath));
        } else if (updatingReport){
            savePictures(Long.valueOf(userRepository.findUserByChatId(userId).get().getTemp()), userId, prepareForSavingReportPictures(fullFilePath));

        } else {
            savePictures(getLastReportId(userId), userId, prepareForSavingReportPictures(fullFilePath));
        }
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

        Report report = null;
        Long userIdInRepository = repoService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("User not found")).getId();
        Collection<Report> reports = reportsRepository.findAllByUserId(userIdInRepository);
        if (reports != null) {
            lastReportIndex = reports.stream()
                    .max(Comparator.comparing(Report::getReportDate))
                    .get().getId();
            if (reportsRepository.existsById(lastReportIndex)) {
                report = reportDao.get(lastReportIndex).get();
            }
        } else {
            report = createDefaultReport(userId);
            lastReportIndex = report.getId();
        }

        assert report != null;
        LocalDateTime newReportCreationTime = report.getReportDate().plusHours(15);

        // Здесь выполняется проверка, прошло ли более 15 часов с момента написания последнего отчета, если прошло, то выполняется
        //создание нового отчета
        if (LocalDateTime.now().isAfter(newReportCreationTime)) {
            report = createDefaultReport(userId);
            lastReportIndex = report.getId();
        }

        return lastReportIndex;
    }

    /**
     * Метод возвращает сообщение о необходимости добавить текст отчета или фото отчета или подтверждение об успешном создании отчета
     * @param chatId id юзера
     * @return сообщение
     */
    @Override
    public String checkNewReportByUser(Long chatId) {
        String result = "";
        if (reportDao.get(getLastReportId(chatId)).isPresent()) {
            Report report = reportDao.get(getLastReportId(chatId)).get();
        if (report.getReportText().startsWith("Empty")) {
            result = "Напишите текст отчета";
        } else if (reportPictureDao.getReportPicturesByReport(report).size() == 0) {
            result = "Пришлите фотографию питомца";
        } else {
            result = "Спасибо, Ваш отчет принят.";
        }
        }
        return result;
    }

    private Report createDefaultReport(Long chatId) {
        Report report = new Report();
        report.setReportText("Empty");
        report.setReportDate();
        report.setUser(userRepository.findUserByChatId(chatId).get());
        report.setReadStatus(Report.ReadStatus.UNREAD);
        report.setDefaultStatus();
        return reportsRepository.saveAndFlush(report);
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
    public String updateReportText(Long reportId, String updatedText) {
        Report report = reportDao.get(reportId).orElseThrow(() -> new ReportNotFoundException("Отчет с таким id не найден!"));
        String reportText = "";
        if (report.getReportText().equals("Empty")) {
            reportText = updatedText;
        } else {
            reportText = report.getReportText() + "\n " + "Upd: " + "\n " + updatedText;
        }

        report.setReportText(reportText);
        report.setStatus(Report.Status.UPDATED);
        report.setReportUpdateDate();
        reportsRepository.save(report);
        reportsRepository.flush();
        return reportText;
    }

    @Override
    public String updateReport(Long userId, String text) throws ReportNotFoundException {
        Long reportId = Long.valueOf(userRepository.findUserByChatId(userId).get().getTemp());
        return updateReportText(reportId, text);
    }

    /**
     * Метод возвращает коллекцию непрочитанных отчетов и отчетов, отправленных на доработку
     * @return список отчетов
     */
    @Override
    public Collection<Report> getUnreadReports() {
        Collection<Report> reports = reportsRepository.findAll();
        if (checkReports()) {
            reports.removeIf(report -> report.getReadStatus() == Report.ReadStatus.READ);
        }
        return reports;
    }

    /**
     * Метод проверяет все отчеты на наличие непрочитанных/отправленных на доработку
     * @return возвращает true, если есть хотя бы один непрочитанный или отправленный на доработку отчет.
     */
    private boolean checkReports() {
       Collection<Report> reports = reportsRepository.findAll();
        int unreadReportsCount = 0;
        for (Report report : reports) {
            if (report.getReadStatus() == Report.ReadStatus.UNREAD || report.getReadStatus() == Report.ReadStatus.TO_BE_UPDATED) {
                unreadReportsCount++;
            }
            if (report.getReadStatus() == null) {
                report.setReadStatus(Report.ReadStatus.UNREAD);
                reportsRepository.save(report);
                unreadReportsCount++;
            }
        }
        reportsRepository.flush();
        return unreadReportsCount > 0;
    }

    /**
     * Метод считает количество фотографий отчета
     * @param report Отчет
     * @return количетсво фотографий
     */
    @Override
    public int getNumberOfPicturesByReport(Report report) {

        return getReportPicturesNames(report.getId()).size();
    }

    /**
     * Метод создает копию отчета для того, чтобы обновить в нем данные: текст или фото.
     * @param report Отчет
     * @return обновленный отчет
     */
    @Override
    public Report createUpdatedReport(Report report) {

        Report updatedReport = new Report();

        updatedReport.setReportText(report.getReportText());
        updatedReport.setReportDate(report.getReportDate());
        updatedReport.setUser(report.getUser());
        updatedReport.setStatus(report.getStatus());
        updatedReport.setReadStatus(report.getReadStatus());

        Report savedReport = saveReport(updatedReport);

        Collection<ReportPicture> pictures = getReportPicturesByReportId(report.getId());
        pictures.forEach(picture -> picture.setReport(savedReport));
        picturesRepository.saveAll(pictures);

        List<PictureName> names = new ArrayList<>(pictureNameRepository.findAllByReportId(report.getId()));
        names.forEach(name -> name.setReport(savedReport));
        pictureNameRepository.saveAll(names);

        reportsRepository.delete(report);
        reportsRepository.flush();
        return savedReport;
    }

    @Override
    public void checkReportsAndSendNotification() {
        List<User> parents = userRepository.findAllParents();
        List<User> volunteers = userRepository.findAllVolunteers();

        for (User parent : parents) {
            Report report = getLastReportByUserId(parent.getChatId());
            if (shouldHaveReport(parent.getChatId()) && report.getReportText() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime dateToCheck = report.getReportDate();
                Period timeDiff = Period.between(now.toLocalDate(), dateToCheck.toLocalDate());
                if (timeDiff.getDays() > 1) {
                    telegramBot.execute(new SendMessage(parent.getChatId(), "Напоминаем о необходимости прислать ежедневный отчет о питомце. \n В тексте отчета укажите:\n- Рацион животного\n" +
                            "- Общее самочувствие и привыкание к новому месту\n" +
                            "- Изменение в поведении: отказ от старых привычек, приобретение новых\n" +
                            "- Фото животного"));
                    for (User volunteer : volunteers) {
                        telegramBot.execute(new SendMessage(volunteer.getChatId(), "Родитель " + parent.getName() + " не присылал отчет более суток!"));
                    }
                }
            } else {

                    telegramBot.execute(new SendMessage(parent.getChatId(), "Напоминаем о необходимости прислать ежедневный отчет о питомце. \n В тексте отчета укажите:\n- Рацион животного\n" +
                            "- Общее самочувствие и привыкание к новому месту\n" +
                            "- Изменение в поведении: отказ от старых привычек, приобретение новых\n" +
                            "- Фото животного"));
                    for (User volunteer : volunteers) {
                        telegramBot.execute(new SendMessage(volunteer.getChatId(), "Родитель " + parent.getName() + " еще не прислал ни одного отчета!"));
                    }

            }
        }

    }

    @Scheduled(cron = "@hourly")
//    @Scheduled(fixedDelay = 60000)
    @Override
    public void scheduledCheckReports() {
        checkReportsAndSendNotification();
    }

    @Override
    public Report getLastReportByUserId(Long chatId) {
            return reportsRepository.findAllByUserChatId(chatId)
                    .stream()
                    .max(Comparator.comparing(Report::getId))
                    .orElse(new Report());
    }

    private boolean shouldHaveReport(Long chatId) {
        User parent = userRepository.findUserByChatId(chatId).get();
        LocalDate startDate = trialPeriodService.findByUserChatId(parent.getChatId()).getStartDate().toLocalDate();
        LocalDate now = LocalDate.now();
        Period between = Period.between(startDate, now);
        return between.getDays() > 1;
    }
}