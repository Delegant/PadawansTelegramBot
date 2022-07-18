package pro.sky.telegrambot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.IncomingMessage;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.AdministrativeService;
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

    private final UserService userService;

    private final ReportService reportService;

    private final MessageServiceImpl messageService;

    private final TrialPeriodService trialPeriodService;

    public AdministrativeServiceImpl(UserService userService,
                                     ReportService reportService,
                                     MessageServiceImpl messageService,
                                     TrialPeriodService trialPeriodService) {
        this.userService = userService;
        this.reportService = reportService;
        this.messageService = messageService;
        this.trialPeriodService = trialPeriodService;
    }

    private boolean checkVolunteer(Long volunteerId) {
        User volunteer = userService.getUserByChatId(volunteerId).orElseThrow(() -> new UserNotFoundException("!!!! There is no volunteer with such ID"));
        return volunteer.getRole().equals(User.Role.VOLUNTEER);
    }

    private boolean checkUser(Long userId) {
        return userService.getUserByChatId(userId).isPresent();
    }
    @Override
    public void setParent(Long volunteerId, Long userId) {
        User newParent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            newParent.setRole(User.Role.PARENT);
            logger.info("Role of user: {} has been changed to PARENT", newParent );
            startTrialPeriod(volunteerId, userId);
        }
    }

    @Override
    public Collection<Report> getListOfReportsByUserName(Long volunteerId, String userName) {
        Collection<Report> reports = new ArrayList<>();
        if (checkVolunteer(volunteerId)) {
            reports = reportService.getListOfReportsByUserName(userName);
        }
        return reports;
    }

    @Override
    public Collection<Report> getListOfReportsByUserId(Long volunteerId, Long userId) {
        Collection<Report> reports = new ArrayList<>();
        if (checkVolunteer(volunteerId)) {
            reports = reportService.getListOfReportsByUserId(userId);
        }
        return reports;
    }

    @Override
    public List<IncomingMessage> getUnreadIncomingMessages(Long volunteerId) {
        List<IncomingMessage> unreadMessages = new ArrayList<>();
        if (checkVolunteer(volunteerId)) {
           unreadMessages = new ArrayList<>(messageService.getUnreadIncomingMessages());
        }
        return unreadMessages;
    }

    @Override
    public IncomingMessage getUnreadMessage(Long volunteerId, Long messageId) {
        IncomingMessage incomingMessage = new IncomingMessage();
        if (checkVolunteer(volunteerId)) {
            incomingMessage = messageService.getIncomingMessage(messageId);
        }
        return incomingMessage;
    }

    @Override
    public void startTrialPeriod(Long volunteerId, Long parentId) {
        User parent = userService.getUserByChatId(parentId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            trialPeriodService.startTrialPeriod(parentId, volunteerId);
            logger.info("Trial period for User: " + parent + " has been started at " + LocalDateTime.now());
        }
    }
    @Override
    public void applyTrialPeriod(Long volunteerId, Long parentId) {
        User parent = userService.getUserByChatId(parentId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            trialPeriodService.closeTrialPeriod(parentId, volunteerId);
            logger.info("Trial period for Parent: " + parent + " has been approved");
        }
    }

    @Override
    public void prolongTrialPeriod(Long volunteerId, int addedDays, Long parentId) {
        User parent = userService.getUserByChatId(parentId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            trialPeriodService.prolongTrialPeriod(parentId, addedDays, volunteerId);
            logger.info("Trial period of Parent: " + parent + " has been prolonged for " + addedDays + " days");
        }
    }

    @Override
    public void declineTrialPeriod(Long volunteerId, Long parentId) {
        User parent = userService.getUserByChatId(parentId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            trialPeriodService.declineTrialPeriod(parentId, volunteerId);
            logger.info("Trial period for Parent: " + parent + " has been declined");
        }
    }



}
