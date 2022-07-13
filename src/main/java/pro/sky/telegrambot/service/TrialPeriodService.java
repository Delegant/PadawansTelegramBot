package pro.sky.telegrambot.service;

public interface TrialPeriodService {

    void startTrialPeriod(Long userId, Long volunteerId);

    void closeTrialPeriod(Long userId, Long volunteerId);

    void prolongTrialPeriod(Long userId, int addedDays, Long volunteerId);

    void declineTrialPeriod(Long userId, Long volunteerId);
}
