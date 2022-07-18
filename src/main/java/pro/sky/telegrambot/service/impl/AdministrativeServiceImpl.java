package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.IncomingMessage;
import pro.sky.telegrambot.service.AdministrativeService;

import java.util.List;

@Service
public class AdministrativeServiceImpl implements AdministrativeService {

    @Override
    public void setParent(Long volunteerId, Long userId) {

    }

    @Override
    public List<String> getListOfReportsByUserName(Long volunteerId, String userName) {
        return null;
    }

    @Override
    public List<String> getListOfReportsByUserId(Long volunteerId, Long UserId) {
        return null;
    }

    @Override
    public List<IncomingMessage> getUnreadIncomingMessages(Long volulnteerId) {
        return null;
    }

    @Override
    public IncomingMessage getUnreadMessage(Long volunteerId, Long messageId) {
        return null;
    }

    @Override
    public void applyTrialPeriod(Long volunteerId, Long parentId) {

    }

    @Override
    public void prolongTrialPeriod(Long volunteerId, Long parentId) {

    }

    @Override
    public void declineTrialPeriod(Long volunteerId, Long parentId) {

    }
}
