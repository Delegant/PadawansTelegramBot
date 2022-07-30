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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;

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

    @Override
    public String getTrialPeriodInformation(Long userId) {
        TrialPeriod trialPeriod = trialPeriodRepository.findByUserChatId(userId);
        String status = "";
        String result = "";
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate startDate = LocalDate.parse(LocalDateTime.now().toString().substring(0, 10), formatter);
        LocalDate endDate = LocalDate.parse(trialPeriod.getEndDate().toString().substring(0, 10), formatter);
        Period leftDays = Period.between(startDate, endDate);
        String formattedDate = endDate.format(formatter1);

        if (trialPeriod.getStatus() == TrialPeriod.TrialPeriodStatus.STARTED) {
            result = "Ваш испытательный период закончится " + formattedDate + "\n Осталось " + leftDays.getDays() + " дней.";
        } else if (trialPeriod.getStatus() == TrialPeriod.TrialPeriodStatus.DENIED) {
            result = "Ваш испытательный период был отменен!";
        } else if (trialPeriod.getStatus() == TrialPeriod.TrialPeriodStatus.PROLONGED) {
            result = "Ваш испытательный период продлен на " + trialPeriod.getAdditionalDays() + " дней и закончится " + endDate + "\n Осталось " + leftDays + " дней.";
        } else if (trialPeriod.getStatus() == TrialPeriod.TrialPeriodStatus.ENDED) {
            result = "Ваш испытательный период успешно завершился " + endDate;
        }

        return result;
    }
}
