package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.Dao.Impl.TrialPeriodDao;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.TrialPeriod;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.TrialPeriodRepository;
import pro.sky.telegrambot.service.TrialPeriodService;
import pro.sky.telegrambot.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class TrialPeriodServiceImpl implements TrialPeriodService {

    private final TrialPeriodRepository trialPeriodRepository;
    private final UserService userService;

    @Autowired
    private TelegramBot telegramBot;

    private final TrialPeriodDao tpDAO;

    private final Logger logger = LoggerFactory.getLogger(TrialPeriodServiceImpl.class);

    public TrialPeriodServiceImpl(TrialPeriodRepository trialPeriodRepository,
                                  UserService userService,
                                  TrialPeriodDao tpDAO) {
        this.trialPeriodRepository = trialPeriodRepository;
        this.userService = userService;
        this.tpDAO = tpDAO;
    }

    /**
     * Метод запускает испытательный период для юзера, взявшего животное из питомца
     * @param userId id юзера
     * @param volunteerId id волонтера
     */
    @Override
    public void startTrialPeriod(Long userId, Long volunteerId) {
        User parent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! user not found"));
        TrialPeriod trialPeriod = new TrialPeriod(parent, volunteerId);
        Collection<TrialPeriod> periods = new ArrayList<>();
        periods.add(trialPeriod);
        trialPeriodRepository.save(trialPeriod);
        parent.setTrialPeriods(periods);
        userService.updateUser(parent);
        logger.info("Trial period started successfully");
    }

    /**
     * Метод завершает испытательный период. (успешное прохождение испытательного периода)
     * @param userId id юзера
     * @param volunteerId id волонтера
     */
    @Override
    public void closeTrialPeriod(Long userId, Long volunteerId) {
        User parent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! user not found"));
        TrialPeriod period = trialPeriodRepository.getTrialPeriodById(Long.valueOf(parent.getTemp()));
        period.setStatus(TrialPeriod.TrialPeriodStatus.ENDED);
        period.setEndDate(LocalDateTime.now());
        period.setAcceptedBy(volunteerId);
        trialPeriodRepository.saveAndFlush(period);
        userService.clearTemp(parent);
        logger.info("Trial period finished successfully");
    }

    /**
     * Метод продлевает испытательный период на указанное количество дней
     * @param userId id юзера
     * @param volunteerId id волонтера
     * @param addedDays дополнительные дни
     */
    @Override
    public void prolongTrialPeriod(Long userId, int addedDays, Long volunteerId) {
        User parent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! user not found"));
        TrialPeriod period = trialPeriodRepository.getTrialPeriodById(Long.valueOf(parent.getTemp()));
        period.setAdditionalDays(addedDays);
        LocalDateTime newEndDate = period.getEndDate().plusDays(addedDays);
        period.setEndDate(newEndDate);
        period.setStatus(TrialPeriod.TrialPeriodStatus.PROLONGED);
        period.setProlongedBy(volunteerId);
        trialPeriodRepository.saveAndFlush(period);
        userService.clearTemp(parent);
        logger.info("Trial period was prolonged for " + addedDays + " days");
    }

    /**
     * Метод закрывает испытательный период (в случае когда он не пройден)
     * @param userId id юзера
     * @param volunteerId id волонтера
     */
    @Override
    public void declineTrialPeriod(Long userId, Long volunteerId) {
        User parent = userService.getUserByChatId(userId).orElseThrow(() -> new UserNotFoundException("!!!! user not found"));
        TrialPeriod period = trialPeriodRepository.getTrialPeriodById(Long.valueOf(parent.getTemp()));
        period.setStatus(TrialPeriod.TrialPeriodStatus.DENIED);
        period.setDeniedBy(volunteerId);
        period.setEndDate(LocalDateTime.now());
        trialPeriodRepository.saveAndFlush(period);
        userService.clearTemp(parent);
        logger.info("Trial period was cancelled");
    }

    /**
     * Метод возвращает сообщение для пользователя с информацией об испытательном периоде
     * @param userId id юзера
     * @return сообщение
     */
    @Override
    public String getTrialPeriodInformation(Long userId) {
        Collection<TrialPeriod> trialPeriods = trialPeriodRepository.findByUserChatId(userId);
        TrialPeriod trialPeriod = new TrialPeriod();
        if (trialPeriods.size() > 1) {
            trialPeriod = new ArrayList<>(trialPeriods).get(0);
        } else {
            trialPeriod = trialPeriods.stream().max(Comparator.comparing(TrialPeriod::getId)).get();
        }
        String result = "";
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate startDate = LocalDate.parse(LocalDateTime.now().toString().substring(0, 10), formatter);
        LocalDate endDate = LocalDate.parse(trialPeriod.getEndDate().toString().substring(0, 10), formatter);
        Period leftDays = Period.between(startDate, endDate);
        String formattedDate = endDate.format(formatter1);

        if (trialPeriod.getStatus() == TrialPeriod.TrialPeriodStatus.STARTED) {
            result = "Ваш испытательный период " + trialPeriod.getId() + " закончится " + formattedDate + "\n Осталось " + leftDays.getDays() + " дней.";
        } else if (trialPeriod.getStatus() == TrialPeriod.TrialPeriodStatus.DENIED) {
            result = "Ваш испытательный период " + trialPeriod.getId() + " был отменен!";
        } else if (trialPeriod.getStatus() == TrialPeriod.TrialPeriodStatus.PROLONGED) {
            result = "Ваш испытательный период " + trialPeriod.getId() + " продлен на " + trialPeriod.getAdditionalDays() + " дней и закончится " + endDate + "\n Осталось " + leftDays + " дней.";
        } else if (trialPeriod.getStatus() == TrialPeriod.TrialPeriodStatus.ENDED) {
            result = "Ваш испытательный период " + trialPeriod.getId() + " успешно завершился " + endDate;
        }

        return result;
    }

    /**
     * Метод возвращает список из всех испытательных периодов
     * @return список из всех испытательных периодов
     */
    @Override
    public Collection<TrialPeriod> getAllTrialPeriods() {
        return trialPeriodRepository.findAll();
    }

    /**
     * Метод находит испытательный период по id
     * @param periodId id испытательного периода
     * @return испытательный период
     */
    @Override
    public TrialPeriod getById(Long periodId) {
        return trialPeriodRepository.getById(periodId);
    }

    /**
     * Метод возвращает юзера по испытательному периоду
     * @param trialPeriod испытательный период
     * @return юзер
     */
    @Override
    public User getUser(TrialPeriod trialPeriod) {
        return userService.findById(trialPeriodRepository.getUserId(trialPeriod.getId()));
    }

    /**
     * Метод получает все испытательные периоды, исключает из них те, которые завершаются более чем через 24 часа и те, которые завершены
     * и отправляет всем волонтерам сообщение о том, что испытательный период заканчивается
     * (если остался хоть один, удовлетворяющий требованиям, т.е. до конца которого меньше 24 часов)
     * и надо принять решение
     * @param periods коллекция из всех испытательных периодов
     */
    @Override
    public void checkTrialPeriodDate(Collection<TrialPeriod> periods) {
        List<User> volunteers = userService.getVolunteers();
        periods.removeIf(trialPeriod -> trialPeriod.getEndDate().isBefore(LocalDateTime.now()));
        periods.removeIf(trialPeriod -> trialPeriod.getEndDate().minusHours(24).isAfter(LocalDateTime.now()));
        if (periods.size() > 0) {
            for (TrialPeriod period : periods) {
                LocalTime endTime = period.getEndDate().toLocalTime();
                String userName = userService.findById(period.getUserId().getId()).getName();
                long hours = ChronoUnit.HOURS.between(endTime, LocalTime.now());
                for (User volunteer : volunteers) {
                    telegramBot.execute(new SendMessage(volunteer.getChatId(), "Испытательный период родителя " + userName + " закончится через " + hours + " часов. Требуется принять решение!" ));
                }
            }
        }
    }

    /**
     * Метод ежедневно вызывает проверку испытательных периодов.
     * @see #checkTrialPeriodDate(Collection)
     */
    @Scheduled(cron = "@daily")
    public void sendTrialPeriodNotificationMessage(){
        checkTrialPeriodDate(trialPeriodRepository.findAll());
    }

    /**
     * Метод находит испытательный период (последний) по chat_id юзера
     * @param chatId юзер id в Телеграм
     * @return испытательный период
     */
    @Override
    public TrialPeriod findByUserChatId(Long chatId) {
        Collection<TrialPeriod> trialPeriods = trialPeriodRepository.findByUserChatId(chatId);
        TrialPeriod trialPeriod = new TrialPeriod();
        if (trialPeriods.size() > 1) {
            trialPeriod = new ArrayList<>(trialPeriods).get(0);
        } else {
            trialPeriod = trialPeriods.stream().max(Comparator.comparing(TrialPeriod::getId)).get();
        }
        return trialPeriod;
    }
}
