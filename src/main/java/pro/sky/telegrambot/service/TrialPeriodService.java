package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.TrialPeriod;
import pro.sky.telegrambot.model.User;

import java.util.Collection;

public interface TrialPeriodService {

    void startTrialPeriod(Long userId, Long volunteerId);

    void closeTrialPeriod(Long userId, Long volunteerId);

    void prolongTrialPeriod(Long userId, int addedDays, Long volunteerId);

    void declineTrialPeriod(Long userId, Long volunteerId);

    String getTrialPeriodInformation(Long userId);

    Collection<TrialPeriod> getAllTrialPeriods();

    TrialPeriod getById(Long periodId);

    User getUser(TrialPeriod trialPeriod);

    void checkTrialPeriodDate(Collection<TrialPeriod> periods);

    TrialPeriod findByUserChatId(Long chatId);
}
