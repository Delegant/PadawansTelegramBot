package pro.sky.telegrambot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.TrialPeriod;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.TrialPeriodRepository;
import pro.sky.telegrambot.service.TrialPeriodService;
import pro.sky.telegrambot.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class TrialPeriodServiceImpl implements TrialPeriodService {

    private final TrialPeriodRepository trialPeriodRepository;
    private final UserService userService;

    private final Logger logger = LoggerFactory.getLogger(TrialPeriodServiceImpl.class);

    public TrialPeriodServiceImpl(TrialPeriodRepository trialPeriodRepository, UserService userService) {
        this.trialPeriodRepository = trialPeriodRepository;
        this.userService = userService;
    }

    @Override
    public void startTrialPeriod(Long userId, Long volunteerId) {
        User parent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! user not found"));
        TrialPeriod trialPeriod = new TrialPeriod(parent, volunteerId);
        Collection<TrialPeriod> periods = new ArrayList<>();
        periods.add(trialPeriod);
        trialPeriodRepository.save(trialPeriod);
        parent.setTrialPeriods(periods);
        logger.info("Trial period started successfully");
    }

    @Override
    public void closeTrialPeriod(Long userId, Long volunteerId) {
        User parent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! user not found"));
        TrialPeriod period = trialPeriodRepository.findByUser(parent);
        period.setStatus(TrialPeriod.TrialPeriodStatus.ENDED);
        period.setEndDate(LocalDateTime.now());
        period.setAcceptedBy(volunteerId);
        trialPeriodRepository.save(period);
        logger.info("Trial period finished successfully");
    }

    @Override
    public void prolongTrialPeriod(Long userId, int addedDays, Long volunteerId) {
        User parent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! user not found"));
        TrialPeriod period = trialPeriodRepository.findByUser(parent);
        period.setAdditionalDays(period.getEndDate().plusDays(addedDays));
        period.setStatus(TrialPeriod.TrialPeriodStatus.PROLONGED);
        period.setProlongedBy(volunteerId);
        trialPeriodRepository.save(period);
        logger.info("Trial period was prolonged for " + addedDays + " days");
    }

    @Override
    public void declineTrialPeriod(Long userId, Long volunteerId) {
        User parent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! user not found"));
        TrialPeriod period = trialPeriodRepository.findByUser(parent);
        period.setStatus(TrialPeriod.TrialPeriodStatus.DENIED);
        period.setDeniedBy(volunteerId);
        period.setEndDate(LocalDateTime.now());
        trialPeriodRepository.save(period);
        logger.info("Trial period was cancelled");
    }
}
