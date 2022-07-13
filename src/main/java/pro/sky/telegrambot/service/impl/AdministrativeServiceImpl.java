package pro.sky.telegrambot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.IncomingMessage;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.AdministrativeService;
import pro.sky.telegrambot.service.RepoService;
import pro.sky.telegrambot.service.ReportService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdministrativeServiceImpl implements AdministrativeService {

    private Logger logger = LoggerFactory.getLogger(AdministrativeServiceImpl.class);

    private final RepoService userRepositoryService;

    private final RepoService repoService;

    private final ReportService reportService;

    private MessageServiceImpl messageService;

    public AdministrativeServiceImpl(RepoService userRepositoryService,
                                     RepoService repoService,
                                     ReportService reportService,
                                     MessageServiceImpl messageService) {
        this.userRepositoryService = userRepositoryService;
        this.repoService = repoService;
        this.reportService = reportService;
        this.messageService = messageService;
    }

    private boolean checkVolunteer(Long volunteerId) {
        User volunteer = userRepositoryService.getUserByChatId(volunteerId).orElseThrow(() -> new UserNotFoundException("!!!! There is no volunteer with such ID"));
        return volunteer.getRole().equals(User.Role.VOLUNTEER);
    }

    private boolean checkUser(Long userId) {
        return userRepositoryService.getUserByChatId(userId).isPresent();
    }
    @Override
    public void setParent(Long volunteerId, Long userId) {
        User newParent = userRepositoryService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            newParent.setRole(User.Role.PARENT);
            logger.info("Role of user: {} has been changed to PARENT", newParent );
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
    public void applyTrialPeriod(Long volunteerId, Long parentId) {
        User parent = repoService.getUserByChatId(parentId).orElseThrow(() -> new UserNotFoundException("!!!! There is no user with such ID"));
        if (checkVolunteer(volunteerId)) {
            //todo
        }
    }

    @Override
    public void prolongTrialPeriod(Long volunteerId, Long parentId) {

    }

    @Override
    public void declineTrialPeriod(Long volunteerId, Long parentId) {

    }
}
