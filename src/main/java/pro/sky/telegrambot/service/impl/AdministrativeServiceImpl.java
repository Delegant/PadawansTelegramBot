package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.IncomingMessage;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.AdministrativeService;
import pro.sky.telegrambot.service.UserService;
import pro.sky.telegrambot.service.ReportService;
import pro.sky.telegrambot.service.TrialPeriodService;
import pro.sky.telegrambot.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdministrativeServiceImpl implements AdministrativeService {

    private final Logger logger = LoggerFactory.getLogger(AdministrativeServiceImpl.class);

    private final UserService userRepositoryService;

    private final UserService userService;

    private final ReportService reportService;

    private final MessageServiceImpl messageService;

    private final TrialPeriodService trialPeriodService;

    private final UserRepository userRepository;

    private final TelegramBot telegramBot;

    public AdministrativeServiceImpl(UserService userRepositoryService,
                                     UserService userService,
                                     ReportService reportService,
                                     MessageServiceImpl messageService,
                                     TrialPeriodService trialPeriodService,
                                     UserRepository userRepository,
                                     TelegramBot telegramBot) {
        this.userRepositoryService = userRepositoryService;
        this.userService = userService;
        this.reportService = reportService;
        this.messageService = messageService;
        this.trialPeriodService = trialPeriodService;
        this.userRepository = userRepository;
        this.telegramBot = telegramBot;
    }

    /**
     * Проверяет, является ли юзер волонтером
     * @param volunteerId идентификатор волонтера
     * @return true/false
     * @throws UserNotFoundException если юзер не волонтер
     */
    private boolean checkVolunteer(Long volunteerId) {
        User volunteer = userRepositoryService.getUserByChatId(volunteerId)
                .orElseThrow(() -> new UserNotFoundException("!!!! There is no volunteer with such ID"));
        return volunteer.getRole().equals(User.Role.VOLUNTEER);
    }

    /**
     * Проверяет, является ли пользователь админом
     * @param adminId идентификатор админа
     * @return true/false
     * @throws UserNotFoundException если юзер не админ
     */
    private boolean checkIfAdmin(Long adminId) {
        User admin = userRepositoryService.getUserByChatId(adminId)
                .orElseThrow(() -> new UserNotFoundException("!!!! There is no admin with such ID"));

        return admin.getRole().equals(User.Role.ADMIN);
    }


    /**
     * Проверка пользователя на наличие в базе данных
     * @param userId идентификатор волонтера
     * @return true/false
     * @throws UserNotFoundException если юзер не найден
     */
    private boolean checkUser(Long userId) {
        return userService.getUserByChatId(userId).isPresent();
    }

    /**
     * Устанавлявает роль юзера - PARENT
     * @param volunteerId id волонтера
     * @param userId id пользователя
     */
    @Override
    public void setParent(Long volunteerId, Long userId) {
        User newParent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            newParent.setRole(User.Role.PARENT);
            logger.info("Role of user: {} has been changed to PARENT", newParent );
            startTrialPeriod(volunteerId, userId);
        }
    }

    /**
     * Получение всех отчетов по имени пользователя
     * @param volunteerId id волонтера
     * @param userName имя пользователя
     * @return коллекцию отчетов
     */
    @Override
    public Collection<Report> getListOfReportsByUserName(Long volunteerId, String userName) {
        Collection<Report> reports = new ArrayList<>();
        if (checkVolunteer(volunteerId)) {
            reports = reportService.getListOfReportsByUserName(userName);
        }
        return reports;
    }

    /**
     * Получение всех отчетов пользователя по id
     * @param volunteerId id волонтера
     * @param userId id пользователя
     * @return коллекцию отчетов
     */
    @Override
    public Collection<Report> getListOfReportsByUserId(Long volunteerId, Long userId) {
        Collection<Report> reports = new ArrayList<>();
        if (checkVolunteer(volunteerId)) {
            reports = reportService.getListOfReportsByUserId(userId);
        }
        return reports;
    }

    /**
     * Возвращает список непрочитанных входящих сообщений
     * @param volunteerId id волонтера
     * @return список входящих сообщений
     */
    @Override
    public List<IncomingMessage> getUnreadIncomingMessages(Long volunteerId) {
        List<IncomingMessage> unreadMessages = new ArrayList<>();
        if (checkVolunteer(volunteerId)) {
           unreadMessages = new ArrayList<>(messageService.getUnreadIncomingMessages());
        }
        return unreadMessages;
    }

    /**
     * Возвращает входящее сообщение по id
     * @param volunteerId id волонтера
     * @param messageId id сообщения
     * @return входящее сообщение
     */
    @Override
    public IncomingMessage getUnreadMessage(Long volunteerId, Long messageId) {
        IncomingMessage incomingMessage = new IncomingMessage();
        if (checkVolunteer(volunteerId)) {
            incomingMessage = messageService.getIncomingMessage(messageId);
        }
        return incomingMessage;
    }

    /**
     * Запускает испытательный период
     * @param volunteerId id волонтера
     * @param parentId id усыновителя
     */
    @Override
    public void startTrialPeriod(Long volunteerId, Long parentId) {
        User parent = userService.getUserByChatId(parentId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            trialPeriodService.startTrialPeriod(parentId, volunteerId);
            logger.info("Trial period for User: " + parent + " has been started at " + LocalDateTime.now());
        }
    }

    /**
     * Завершает испытательный период (успешно)
     * @param volunteerId id волонтера
     * @param parentId id усыновителя
     */
    @Override
    public void applyTrialPeriod(Long volunteerId, Long parentId) {
        User parent = userService.getUserByChatId(parentId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            trialPeriodService.closeTrialPeriod(parentId, volunteerId);
            logger.info("Trial period for Parent: " + parent + " has been approved");
        }
    }

    /**
     * Продлевает испытательный период на указанное количество дней
     * @param volunteerId id волонтера
     * @param addedDays количество дней для продления
     * @param parentId id усыновителя
     */
    @Override
    public void prolongTrialPeriod(Long volunteerId, int addedDays, Long parentId) {
        User parent = userService.getUserByChatId(parentId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            trialPeriodService.prolongTrialPeriod(parentId, addedDays, volunteerId);
            logger.info("Trial period of Parent: " + parent + " has been prolonged for " + addedDays + " days");
        }
    }

    /**
     * Отмена испытательного периода
     * @param volunteerId id волонтера
     * @param parentId id усыновителя
     */
    @Override
    public void declineTrialPeriod(Long volunteerId, Long parentId) {
        User parent = userService.getUserByChatId(parentId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            trialPeriodService.declineTrialPeriod(parentId, volunteerId);
            logger.info("Trial period for Parent: " + parent + " has been declined");
        }
    }

    /**
     * Назначение пользователя волонтером (выполняется ТОЛЬКО админом)
     * @param adminId id админа
     * @param userId id пользователя
     */
    @Override
    public void setNewVolunteer(Long adminId, Long userId) {
        User user = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkIfAdmin(adminId)) {
            user.setRole(User.Role.VOLUNTEER);
            userRepository.save(user);
            logger.info("**** New volunteer has been added!");
        } else {
            logger.warn("!!!! User with ID: " + adminId + " is not an admin!");
        }
    }

    /**
     * Назначение нового админа (выполняется только АДМИНОМ)
     * @param adminId id админа
     * @param userId id пользователя
     */
    @Override
    public void setNewAdmin(Long adminId, Long userId) {
        User user = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkIfAdmin(adminId)) {
            user.setRole(User.Role.ADMIN);
            userRepository.save(user);
            logger.info("**** New ADMIN has been added!");
        } else {
            logger.warn("!!!! User with ID: " + adminId + " is not an admin!");
        }
    }

    public List<Long> getVolunteers() {

        List<User> volunteers =  userRepository.findAllByRole(User.Role.VOLUNTEER);

        return volunteers.stream().map(User::getChatId).collect(Collectors.toList());
    }

    public void notifyAllVolunteers(List<Long> volunteersId, String messageText) throws InterruptedException {

        for (int i = 0; i < volunteersId.size(); i++) {

            SendMessage message = new SendMessage(i, messageText);
            telegramBot.execute(message).wait(100);
        }
    }
}
