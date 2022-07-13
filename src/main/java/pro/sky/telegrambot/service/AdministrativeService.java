package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.IncomingMessage;

import java.util.List;

public interface AdministrativeService {

    public void setParent(Long volunteerId, Long userId);

    public List<String> getListOfReportsByUserName(Long volunteerId, String userName);

    public List<String> getListOfReportsByUserId(Long volunteerId, Long UserId);

    public List<IncomingMessage> getUnreadIncomingMessages(Long volulnteerId);

    public IncomingMessage getUnreadMessage(Long volunteerId, Long messageId);

    public void applyTrialPeriod(Long volunteerId, Long parentId);

    public void prolongTrialPeriod(Long volunteerId, Long parentId);

    public void declineTrialPeriod(Long volunteerId, Long parentId);
}
