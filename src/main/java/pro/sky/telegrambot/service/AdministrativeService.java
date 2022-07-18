package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.IncomingMessage;
import pro.sky.telegrambot.model.Report;

import java.util.Collection;
import java.util.List;

public interface AdministrativeService {

    void setParent(Long volunteerId, Long userId);

    Collection<Report> getListOfReportsByUserName(Long volunteerId, String userName);

    Collection<Report> getListOfReportsByUserId(Long volunteerId, Long UserId);

    List<IncomingMessage> getUnreadIncomingMessages(Long volulnteerId);

    IncomingMessage getUnreadMessage(Long volunteerId, Long messageId);

    void startTrialPeriod(Long volunteerId, Long userId);

    void applyTrialPeriod(Long volunteerId, Long parentId);

    void prolongTrialPeriod(Long volunteerId, int addedDays, Long parentId);

    void declineTrialPeriod(Long volunteerId, Long parentId);

    void setNewVolunteer(Long adminId, Long userId);

    void setNewAdmin(Long adminId, Long userId);
}
