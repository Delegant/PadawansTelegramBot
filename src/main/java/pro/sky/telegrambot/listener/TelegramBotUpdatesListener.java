package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.logging.log4j.util.TriConsumer;
import org.hibernate.Transaction;
import org.hibernate.bytecode.enhance.spi.interceptor.AbstractLazyLoadInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.constants.ButtonsText;
import pro.sky.telegrambot.exceptions.UnknownUpdateException;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.TrialPeriod;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.*;
import pro.sky.telegrambot.service.impl.UserServiceImpl;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static pro.sky.telegrambot.model.MenuStack.MessageType;
import static pro.sky.telegrambot.model.MenuStack.MessageType.*;
import static pro.sky.telegrambot.model.User.Role;
import static pro.sky.telegrambot.model.User.Role.*;
import static pro.sky.telegrambot.model.User.Role.CHANNEL;

/**
 * Основной класс бота, где происходит обработка входящих обновлений из клиента
 * Имплементирует UpdatesListener в качестве обработчика обновлений
 *
 * @see com.pengrad.telegrambot.UpdatesListener
 */
@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    /**
     * Инжектированный сервис-класс, отвечающий за обработку и создание клавиатур
     *
     * @see MenuService
     */
    private final MenuService menuService;
    /**
     * Сервис репозитория, отвечающий за сохранение пользователей в БД
     *
     * @see UserServiceImpl
     */
    private final UserServiceImpl userService;
    /**
     * Инжектированный сервис-класс, отвечающий за отслеживанием положения
     * пользователей в БД
     *
     * @see MenuService
     */
    private final MenuStackService menuStackService;
    /**
     * Логгер для класса
     */
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    /**
     * поле инжектирует Телеграм-бота
     *
     * @see pro.sky.telegrambot.configuration.TelegramBotConfiguration
     */
    private final TelegramBot telegramBot;
    private final ReportService reportService;
    private final TrialPeriodService trialPeriodService;
    private final AdministrativeService administrativeService;
    /**
     * директория с файлом - схемой проезда к приюту
     */
    java.io.File address = new File("src/main/resources/MapPhoto/address.png");
    Function<String, Boolean> whatIsMenu;
    BiConsumer<String, String> doSendMessage;
    BiConsumer<String, String> doSendCustomMessage;
    TriConsumer<File, String, String> doSendPhoto;
    BiConsumer<Float, Float> goSendLocation;
    BiConsumer<String, String> doSendReportList;
    BiConsumer<String, String> doSendUsersList;
    Consumer<File> goSendPhoto;
    Runnable goBack;
    BiConsumer<String, String> doSetNewVolunteer;
    BiConsumer<String, String> doSendParentReportList;
    BiConsumer<String, String> doSendReport;
    BiConsumer<String, String> doSendNotification;
    TriConsumer<Long, Long, String> doSendUserNotification;
    BiConsumer<Long, String> doSendParentReport;
    @Value("${pro.sky.channel.id}")
    private Long channelId;
    BiConsumer<String, String> doSendTrialPeriodsList;
    BiConsumer<Long, String> doSendTrialPeriod;




    /**
     * конструктор класса
     *
     * @param telegramBot      Телеграм бот
     * @param menuService      обработчик-меню
     * @param userService      сервис репозитория пользователей
     * @param menuStackService сервис репозитория положения юзера в меню
     */
    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      MenuService menuService,
                                      UserServiceImpl userService,
                                      MenuStackService menuStackService,
                                      ReportService reportService,
                                      AdministrativeService administrativeService,
                                      TrialPeriodService trialPeriodService) {
        this.telegramBot = telegramBot;
        this.menuService = menuService;
        this.userService = userService;
        this.reportService = reportService;
        this.menuStackService = menuStackService;
        this.administrativeService = administrativeService;
        this.trialPeriodService = trialPeriodService;
    }

    /**
     * Метод, запускающий (инициализирующий обработчик обновлений)
     */
    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    /**
     * Основной метод класса, в котором происходит обработка обновлений
     *
     * @param updates Обновления, поступившие от бота
     * @return подтверждение обработки обновлений
     */
    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("====Processing update: {}", update);
            Message message = (update.message() != null) ? update.message() : update.callbackQuery().message();
            User currentUser = userService.getUserByMessage(message);
            Role roleCurrentUser = currentUser.getRole();
            String textPackKey = menuStackService.getLastTextPackKeyByUser(currentUser);
            MessageType expectedTypeCurrentMessage = menuStackService.getCurrentExpectedMessageTypeByUser(currentUser);
            MessageType realTypeCurrentMessage = getCurrentMessageType(expectedTypeCurrentMessage, update, message);
            menuStackService.createMenuStack(currentUser, textPackKey, expectedTypeCurrentMessage);
            ButtonsText buttonsText = ButtonsText.getButtonText(textPackKey);
            logger.info("====Received message {} is processed as {} and from {}", message.text(), realTypeCurrentMessage, roleCurrentUser);
                try {
                    if (checkStartCommand(update)) {
                        switch (roleCurrentUser) {
                            case VOLUNTEER:
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                                functionalInitForTextCommand(message, buttonsText);
                                handleVolunteerMessages(whatIsMenu, doSendMessage, doSendUsersList, doSendReportList, doSendReport, currentUser, update);
                                break;
                            case PARENT:
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                                functionalInitForTextCommand(message, buttonsText);
                                handleParentMessages(whatIsMenu, doSendMessage, doSendPhoto, goSendLocation, currentUser, update, buttonsText);
                                break;
                            case ADMIN:
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                                functionalInitForTextCommand(message, buttonsText);
                                handleAdminMessages(whatIsMenu, doSendMessage, goSendPhoto, goBack, doSetNewVolunteer, doSendUsersList, doSendReport, currentUser, update);
                                break;
                            case USER:
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                                functionalInitForTextCommand(message, buttonsText);
                                handleUserMessages(whatIsMenu, doSendMessage, doSendPhoto, goSendLocation, currentUser, update, buttonsText);
                                break;
                        }
                    }
                    else if (expectedTypeCurrentMessage == COMMAND || expectedTypeCurrentMessage == COMMAND_CALL_BACK) {
                        if (realTypeCurrentMessage == COMMAND_CALL_BACK) {
                            functionalInitForCallBack(update, buttonsText, currentUser);
                        }
                        else {
                            functionalInitForTextCommand(message, buttonsText);
                        }
                        if (roleCurrentUser == USER) {
                            handleUserMessages(whatIsMenu, doSendMessage, doSendPhoto, goSendLocation, currentUser, update, buttonsText);
                        }
                        else if (roleCurrentUser == PARENT) {
                            handleParentMessages(whatIsMenu, doSendMessage, doSendPhoto, goSendLocation, currentUser, update, buttonsText);
                        }
                        else if (roleCurrentUser == VOLUNTEER) {
                            handleVolunteerMessages(whatIsMenu, doSendMessage, doSendUsersList, doSendReportList, doSendReport, currentUser, update);
                        }
                        else if (roleCurrentUser == ADMIN) {
                            handleAdminMessages(whatIsMenu, doSendMessage, goSendPhoto, goBack, doSetNewVolunteer, doSendUsersList, doSendReport, currentUser, update);
                        }
                        else if (roleCurrentUser == CHANNEL) {
                            String command = update.callbackQuery().data();
                            if (command.startsWith(buttonsText.getString("BEGIN_PREFIX"))) {
                                String chatId = command.substring(buttonsText.getString("BEGIN_PREFIX").length());
                                beginOrFinishDialog(update,
                                        chatId,
                                        DIALOG,
                                        buttonsText.getString("CHAT_BEGIN_DIALOG_TEXT"),
                                        buttonsText.getMenu("TO_SUPPORT_DENY_MENU"),
                                        List.of(buttonsText.getString("FINISH_PREFIX") + chatId));
                            } else if (command.startsWith(buttonsText.getString("FINISH_PREFIX"))) {
                                String chatId = command.substring(buttonsText.getString("FINISH_PREFIX").length());
                                beginOrFinishDialog(update,
                                        chatId,
                                        COMMAND,
                                        buttonsText.getString("CHAT_FINISH_DIALOG_TEXT"),
                                        null,
                                        null);
                            }
                        }
                    }
                    else if (realTypeCurrentMessage == REPORT_LIST && update.callbackQuery() != null) {
                        functionalInitForCallBack(update, buttonsText, currentUser);
                        if (roleCurrentUser == PARENT) {
                            doSendReport.accept(update.callbackQuery().data(), "INSIDE_PARENT_REPORT_MENU");
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, READING_REPORT);
                        }
                        doSendReport.accept(update.callbackQuery().data(), "INSIDE_REPORT_MENU");
                        menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, READING_REPORT);
                    }
                    else if (realTypeCurrentMessage == REPORT_ACTION && update.callbackQuery() != null) {
                        functionalInitForCallBack(update, buttonsText, currentUser);
                        if (roleCurrentUser == PARENT) {
                            if (callBackChecker(update) == 0) {
                                currentUser.setTemp(getIdFromCallback(update).toString());
                                userService.updateUser(currentUser);
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, RECEIVED_REPORT_NOTIFICATION);
                                doSendMessage.accept("ASK_TO_SEND_PIC", "IN_REPORT_SEND_TEXT_MENU");
                            } else if (callBackChecker(update) == 1) {
                                currentUser.setTemp(getIdFromCallback(update).toString());
                                userService.updateUser(currentUser);
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, RECEIVED_REPORT_NOTIFICATION);
                                doSendMessage.accept("ASK_TO_SEND_TEXT", "IN_REPORT_SEND_TEXT_MENU");

                            } else {
                                handleParentMessages(whatIsMenu, doSendMessage, doSendPhoto, goSendLocation, currentUser, update, buttonsText);
                            }

                        }
                        else {
                            if (callBackChecker(update) == 0) {
                                Report report = reportService.getReportById(getIdFromCallback(update));
                                report.setReadStatus(Report.ReadStatus.TO_BE_UPDATED);
                                reportService.saveReport(report);
                                doSendUserNotification.accept(report.getUser().getChatId(), report.getId(), "Пришлите фотографию для отчета с номером " + report.getId());

                            } else if (callBackChecker(update) == 1) {
                                Report report = reportService.getReportById(getIdFromCallback(update));
                                report.setReadStatus(Report.ReadStatus.TO_BE_UPDATED);
                                reportService.saveReport(report);
                                doSendUserNotification.accept(report.getUser().getChatId(), report.getId(), "Дополните текст отчета с номером " + report.getId());

                            } else if (callBackChecker(update) == 2) {
                                Report report = reportService.getReportById(getIdFromCallback(update));
                                report.setReadStatus(Report.ReadStatus.READ);
                                reportService.saveReport(report);
                                telegramBot.execute(new SendMessage(report.getUser().getChatId(), "Отчет с номером  " + report.getId() + " принят волонтером " + currentUser.getName() + "."));
                            } else {
                                handleParentMessages(whatIsMenu, doSendMessage, doSendPhoto, goSendLocation, currentUser, update, buttonsText);
                            }
                        }
                    }
                    else if (realTypeCurrentMessage == RECEIVED_REPORT_NOTIFICATION) {
                        if (update.message().photo() != null) {
                            reportService.getPictureFromMessage(currentUser.getChatId(), update.message());
                            functionalInitForCallBack(update, buttonsText, currentUser);
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                            currentUser.setTemp(null);
                            userService.updateUser(currentUser);
                            doSendMessage.accept("SEND_REPORT_IS_CREATED", "MAIN_MENU");
                        } else if (update.message().text() != null) {
                            reportService.updateReport(currentUser.getChatId(), update.message().text());
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                            currentUser.setTemp(null);
                            userService.updateUser(currentUser);
                            doSendMessage.accept("SEND_REPORT_IS_CREATED", "MAIN_MENU");
                        } else {
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                            handleParentMessages(whatIsMenu, doSendMessage, doSendPhoto, goSendLocation, currentUser, update, buttonsText);
                        }
                    }
                    else if (realTypeCurrentMessage == UPDATING_REPORT) {
                        if (update.message().photo() != null) {
                            reportService.getPictureFromMessage(currentUser.getChatId(), update.message());
                            if (reportService.checkNewReportByUser(currentUser.getChatId()).startsWith("Спасибо")) {
                                functionalInitForCallBack(update, buttonsText, currentUser);
                                doSendMessage.accept("ASK_TO_SEND_TEXT", "IN_REPORT_SEND_PIC_MENU");
                            }
                            else if (reportService.checkNewReportByUser(currentUser.getChatId()).startsWith("Спасибо")) {
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                                doSendMessage.accept("SEND_REPORT_IS_CREATED", "MAIN_MENU");
                            }
                        }
                        else if (update.message().text() != null) {
                            reportService.updateReport(currentUser.getChatId(), update.message().text());
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                            doSendMessage.accept("SEND_REPORT_IS_CREATED", "MAIN_MENU");
                        }


                    }
                    else if (realTypeCurrentMessage == MY_REPORTS) {
                        functionalInitForCallBack(update, buttonsText, currentUser);
                        //todo Написать функцию для отправки отчета родителю (сейчас логика doSendReport работает для волонтера)
                        doSendReport.accept(update.callbackQuery().data(), "INSIDE_PARENT_REPORT_MENU");

                        menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, READING_REPORT);
                    }
                    else if (realTypeCurrentMessage == REPORT_PIC || realTypeCurrentMessage == REPORT_TEXT) {
                        if (realTypeCurrentMessage == REPORT_PIC) {
                            functionalInitForTextCommand(message, buttonsText);
                            if (roleCurrentUser == PARENT) {
                                reportService.getPictureFromMessage(currentUser.getChatId(), update.message());
                                if (reportService.checkNewReportByUser(currentUser.getChatId()).startsWith("Напишите")) {
                                    doSendMessage.accept("ASK_TO_SEND_TEXT", "IN_REPORT_SEND_PIC_MENU");
                                }
                                else if (reportService.checkNewReportByUser(currentUser.getChatId()).startsWith("Спасибо")) {
                                    menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                                    doSendMessage.accept("SEND_REPORT_IS_CREATED", "MAIN_MENU");
                                }
                            }
                        }
                        else {
                            functionalInitForTextCommand(message, buttonsText);
                            if (roleCurrentUser == PARENT) {
                                Report report = reportService.saveReport(currentUser.getChatId(), update.message().text());
                                if (reportService.getReportPicturesNames(report.getId()).size() == 0) {
                                    doSendMessage.accept("ASK_TO_SEND_PIC", "IN_REPORT_SEND_TEXT_MENU");
                                }
                                else {
                                    menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                                    doSendMessage.accept("SEND_REPORT_IS_CREATED", "MAIN_MENU");
                                }
                            }
                        }
                    }
                    else if (expectedTypeCurrentMessage == REPORT && realTypeCurrentMessage == COMMAND_TEXT) {
                        functionalInitForCallBack(update, buttonsText, currentUser);

                        if (whatIsMenu.apply("FINISH_SENDING_REPORT")) {
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                            doSendMessage.accept("SEND_REPORT_IS_CREATED", "MAIN_MENU");
                        }
                        else if (whatIsMenu.apply("SEND_REPORT_TEXT_BUTTON")) {
                            telegramBot.execute(new SendMessage(currentUser.getChatId(), "ASK_TO_SEND_TEXT"));
                        }
                        else if (whatIsMenu.apply("BACK_TO_MAIN_MENU_BUTTON")) {
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAIN_MENU");
                        }
                        else {
                            handleVolunteerMessages(whatIsMenu, doSendMessage, doSendUsersList, doSendReportList, doSendReport, currentUser, update);
                        }
                    }
                    else if (realTypeCurrentMessage == DIALOG_TEXT) {
                        if (roleCurrentUser == VOLUNTEER || roleCurrentUser == USER || roleCurrentUser == PARENT) {
                            telegramBot.execute(menuService.sendTextLoader(currentUser.getCompanion(), update.message().text()));
                        }
                    }
                        else if (realTypeCurrentMessage == USER_NAME) {
                            functionalInitForCallBack(update, buttonsText, currentUser);
                            doSendUsersList.accept("CHOOSE_USER_TO_MAKE_PARENT", "BACK_TO_VOLUNTEERS_MENU");
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, ADDING_PARENT);
                        }
                        else if (realTypeCurrentMessage == ADDING_PARENT) {
                            functionalInitForCallBack(update, buttonsText, currentUser);
                            User newParent = userService.getUser(Long.valueOf(update.callbackQuery().data()));
                            administrativeService.setParent(currentUser.getChatId(), newParent.getChatId());
                            telegramBot.execute(new SendMessage(newParent.getChatId(), "Поздравляем, вы взяли питомца. Ваш испытательный период начался!"));
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                            doSendMessage.accept("AFTER_ADDING_PARENT", "VOLUNTEER_MAIN_MENU");
                        }
                        else if (realTypeCurrentMessage == TRIAL_PERIOD_LIST) {
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, TRIAL_PERIOD);
                            doSendTrialPeriod.accept(Long.valueOf(update.callbackQuery().data()), "TRIAL_PERIOD_MENU");
                        }
                        else if (realTypeCurrentMessage == TRIAL_PERIOD) {
                            TrialPeriod period = trialPeriodService.getById(Long.valueOf(update.callbackQuery().data()));
                            User parent = trialPeriodService.getUser(period);
                            if (callBackChecker(update) == 3) {
                                administrativeService.applyTrialPeriod(currentUser.getChatId(), parent.getChatId());
                            }
                            else if (callBackChecker(update) == 4) {

                            }
                            else if (callBackChecker(update) == 5) {

                            }
                            else {
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                                handleVolunteerMessages(whatIsMenu, doSendMessage, doSendUsersList, doSendReportList, doSendReport, currentUser, update);

                            }
                        }
                } catch (Exception e) {
                    logger.warn("====Exception: ", e);
                }
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }



    /**
     * Метод для начала или завершения диалога между волонтером и пользователем
     *
     * @param update - текущий update
     * @param chatId      - идентификатор пользователя, запросившего поддержку.
     * @param messageType - тип MessageType при старте диалога - DIALOG и COMMAND при завершении
     * @param text - текст отправляемого сообщения
     * @param menu - отправляемое меню
     * @param callBack - данные callBack, которые вернуться по нажатию кнопок меню
     */
    private void beginOrFinishDialog(Update update, String chatId, MessageType messageType, String text, List<String> menu, List<String> callBack) {
        Long userHelpRequestChatId = Long.valueOf(chatId);
        User userHelpRequest = userService.getUserByChatId(userHelpRequestChatId).orElseThrow(UserNotFoundException::new);
        User userHelpResponse = userService.getUserByChatId(update.callbackQuery().from().id()).orElseThrow(UserNotFoundException::new);
        userService.setCompanion(userHelpResponse, userHelpRequest);
        menuStackService.setCurrentExpectedMessageTypeByUser(userHelpResponse, messageType);
        menuStackService.setCurrentExpectedMessageTypeByUser(userHelpRequest, messageType);
        if (menu != null && callBack != null) {
            telegramBot.execute(menuService.editMenuLoader(update, text + userHelpRequest.getName(), menu, callBack));
        } else {
            telegramBot.execute(menuService.editMenuLoader(update, text + userHelpRequest.getName()));
        }
    }

    /**
     * Метод инициализирует функциональные интерфейсы в ситуации для update с текстом callBack.
     * Применяются интерфейсы как действия по отношению к ключам в методе для пользователя:
     * {@link #handleUserMessages(Function, BiConsumer, TriConsumer, BiConsumer, User, Update, ButtonsText)}
     * и в методе для волонтера
     * {@link #handleVolunteerMessages(Function, BiConsumer, BiConsumer, BiConsumer, BiConsumer, User, Update)}
     */
    private void functionalInitForCallBack(Update update, ButtonsText buttonsText, User currentUser) {
        whatIsMenu = (someButtonNameKey) -> {
            String someButtonNameValue = buttonsText.getString(someButtonNameKey);
            String hashSomeButton = menuService.getHashFromButton(someButtonNameValue);
            if (update.callbackQuery() == null) {
                return false;
            }
            return update.callbackQuery().data().equals(hashSomeButton);
        };
        doSendMessage = (textKey, menuKey) -> {
            logger.info("====Processing doSendMessage with callback: {}", update.callbackQuery().data());
            String textPackKey = menuStackService.getLastTextPackKeyByUser(currentUser);
            MessageType expectedTypeCurrentMessage = menuStackService.getCurrentExpectedMessageTypeByUser(currentUser);
            menuStackService.createMenuStack(currentUser, textPackKey, textKey, menuKey, expectedTypeCurrentMessage);
            String textValue = buttonsText.getString(textKey);
            List<String> menuValue = buttonsText.getMenu(menuKey);
            telegramBot.execute(menuService.editMenuLoader(update, textValue, menuValue));
        };
        doSendPhoto = (filePath, textKey, buttonsValue) -> {
            logger.info("====Processing update with callback: {}", update.callbackQuery().data());
            List<String> buttons = buttonsText.getMenu(buttonsValue);
            String textValue = buttonsText.getString(textKey);
            telegramBot.execute(menuService.sendLocationPhotoLoader(update, filePath, textValue, buttons));
        };
        goSendLocation = (latitude, longitude) -> {
            logger.info("====Processing goSendLocation with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.sendLocationLoader(update, latitude, longitude));
        };
        doSendReportList = (textKey, menuKey) -> {
            logger.info("==== Sending list of Reports: {}", update.callbackQuery().data());
            List<List<String>> buttons = menuService.generateListOfLastReports();
            String text = "Список последних отчетов: ";
            telegramBot.execute(menuService.menuLoaderForObjects(update, text, buttons));
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, REPORT_REQUEST);
        };
        doSendReport = (textValue, menuKey) -> {
            logger.info("==== Getting report with id {}", update.callbackQuery().data());
            Report report = reportService.getReportById(Long.valueOf(textValue));
            String text = report.getReportText();
            List<String> menuValue = buttonsText.getMenu(menuKey);
            if (reportService.ifHasPhoto(report)) {
                menuService.multiplePhotoSend(currentUser.getChatId(), report.getId());
            }
            telegramBot.execute(menuService.sendTextWithMarkedCallBack(currentUser, text, report.getId()));
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, REPORT_ACTION);
        };
        doSendUsersList = (textKey, menuKey) -> {
            logger.info("==== Getting list of users with name like {}", update.message().text());
            List<List<String>> listOfUsers = menuService.generateListOfUsers(update.message().text());
            telegramBot.execute(menuService.menuLoaderForObjects(update.message(), "Выберите пользователя для назначения усыновителем", listOfUsers));
        };
        doSendNotification = (text, menuKey) -> {
            logger.info("==== Sending notification message to user");
            Long userId = Long.valueOf(update.callbackQuery().data());
            List<String> menuValue = buttonsText.getMenu(menuKey);
            telegramBot.execute(menuService.editMenuLoader(update, text, menuValue));
        };
        doSendUserNotification = (userId, reportId, text) -> {
            logger.info("==== Sending notification message to user");
            telegramBot.execute(menuService.sendReportNotificationMessage(userId, reportId, text));
        };
        doSendCustomMessage = (textToSend, menuKey) -> {
            logger.info("==== Sending custom message to user with ig = {}", currentUser.getChatId());
            List<String> menuValue = buttonsText.getMenu(menuKey);
            telegramBot.execute(menuService.editMenuLoader(update, textToSend, menuValue));
        };
        doSendParentReportList = (textToSend, menuKey) -> {
            ///TODO Поправить тип ожидаемого сообщения и как следствие выводимое меню для парента (сейчас выводит как для волонтера)
            logger.info("==== Sending list of Reports: {}", update.callbackQuery().data());
            List<List<String>> buttons = menuService.generateListOfAllUserReports(currentUser.getChatId());
            String text = buttonsText.getString(textToSend);
            if (buttons.size() == 0) {
                telegramBot.execute(menuService.menuLoader(update, "Вы не написали еще ни одного отчета!", buttonsText.getMenu("MAIN_MENU")));
                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
            } else {
                if (Objects.equals(menuKey, "ALL")) {
                    telegramBot.execute(menuService.menuLoaderForObjects(update, text, menuService.generateListOfAllUserReports(currentUser.getChatId())));
                } else {
                    telegramBot.execute(menuService.menuLoaderForObjects(update, text, menuService.generateListOfUpdateRequestedUserReports(currentUser.getChatId())));
                }
                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, MY_REPORTS);
            }
        };
        doSendParentReport = (reportId, menuKey) -> {
            logger.info("==== Sending report to parent with id: {}", update.callbackQuery().data());
            Report report = reportService.getReportById(reportId);
            String text = report.getReportText();
            List<String> menuValue = buttonsText.getMenu(menuKey);
            if (reportService.ifHasPhoto(report)) {
                menuService.multiplePhotoSend(currentUser.getChatId(), report.getId());
            }
            telegramBot.execute(menuService.sendTextWithMarkedCallBack(currentUser, text, report.getId()));
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, REPORT_ACTION);
        };
        doSendTrialPeriodsList = (menuKey, textKey) -> {
            logger.info("==== Sending list of Trial_Periods: {}", update.callbackQuery().data());
            List<List<String>> buttons = menuService.generateListOfAllTrialPeriods();
            String menuText = buttonsText.getString(menuKey);
            if (buttons.size() == 0) {
                telegramBot.execute(menuService.menuLoader(update, "Список испытательных периодов пуст!", buttonsText.getMenu("VOLUNTEER_MAIN_MENU")));
                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
            } else {
                telegramBot.execute(menuService.menuLoaderForObjects(update, menuText, buttons));
                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, TRIAL_PERIOD_LIST);
            }
        };
        doSendTrialPeriod = (trialPeriodId, menuKey) -> {
            logger.info("==== Sending Trial_Period: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.sendTrialPeriod(currentUser, trialPeriodId));
            currentUser.setTemp(update.callbackQuery().data());
            userService.updateUser(currentUser);
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, TRIAL_PERIOD);
        };
    }

    /**
     * Метод инициализирует функциональные интерфейсы в ситуации для update с текстовой командой.
     * Применяются интерфейсы как действия по отношению к ключам в методе для пользователя:
     * {@link #handleUserMessages(Function, BiConsumer, TriConsumer, BiConsumer, User, Update, ButtonsText)}
     * и в методе для волонтера {@link #handleVolunteerMessages(Function, BiConsumer, BiConsumer, BiConsumer, BiConsumer, User, Update)}
     */
    private void functionalInitForTextCommand(Message message, ButtonsText buttonsText) {
        whatIsMenu = (someButtonName) -> message.text().equals(buttonsText.getString(someButtonName));
        doSendMessage = (textKey, menuKey) -> {
            logger.info("====Processing update with text message: {}, log from functionalInitForTextCommand()", message.text());
            List<String> menuValue = buttonsText.getMenu(menuKey);
            String textValue = buttonsText.getString(textKey);
            telegramBot.execute(menuService.menuLoader(message, textValue, menuValue));
        };
    }

    /**
     * Метод принимает тип ожидаемого update, сравнивает с параметрами полученного update
     * и в результате формирует уточненный тип update. По умолчанию тип COMMAND
     * и задан в модели {@link pro.sky.telegrambot.model.MenuStack}.
     * Ожидаем текстовую команду (COMMAND), получаем текст начинающийся с "/" - это COMMAND_TEXT.
     * Ожидаем текстовую команду (COMMAND), получаем текст не начинающийся с "/" - это TEXT.
     * Ожидаем текстовую команду (COMMAND), получаем не текст - это UnknownUpdateException.
     * Ожидаем команду с кнопки (COMMAND_CALL_BACK), получаем текст начинающийся с "/" - это COMMAND_TEXT.
     * Ожидаем команду с кнопки (COMMAND_CALL_BACK), получаем текст - это TEXT.
     * @throws UnknownUpdateException при получении "не текста"
     */
    private MessageType getCurrentMessageType(MessageType messageType, Update update, Message message) {
        User currentUser = null;
        if (update.message() != null) {
            currentUser = userService.getUser(update.message().chat().id());
        } else if (update.callbackQuery() != null) {
            currentUser = userService.getUser(update.callbackQuery().message().chat().id());
        }
        try {
            logger.info("====Parsing message type: {}", messageType.toString());
            if (messageType == COMMAND && update.message() != null) {
                return COMMAND_TEXT;
            } else if (messageType == COMMAND_CALL_BACK && update.callbackQuery().message() != null) {
                return COMMAND_CALL_BACK;
            } else if (messageType == COMMAND && update.callbackQuery().message() != null) {
                return COMMAND_CALL_BACK;
            } else if (messageType == REPORT && update.message() != null && message.photo() == null) {
                return REPORT_TEXT;
            } else if (messageType == REPORT && message.photo() != null) {
                return REPORT_PIC;
            } else if (messageType == DIALOG && update.message() != null) {
                return DIALOG_TEXT;
            } else if (messageType == DIALOG && update.callbackQuery() != null) {
                return COMMAND_CALL_BACK;
            } else if (messageType == REPORT_REQUEST && update.callbackQuery() != null) {
                return REPORT_LIST;
            } else if (messageType == READING_REPORT && update.callbackQuery() != null) {
                return REPORT_ACTION;
            } else if (messageType == USER_NAME && update.message().text() != null) {
                return USER_NAME;
            }
            else if (messageType == ADDING_PARENT && update.callbackQuery() != null) {
                return ADDING_PARENT;
            } else if (messageType == RECEIVED_REPORT_NOTIFICATION) {
                return RECEIVED_REPORT_NOTIFICATION;
            } else if (messageType == UPDATING_REPORT && update.message() != null) {
                return UPDATING_REPORT;
            } else if (messageType == MY_REPORTS) {
                return MY_REPORTS;
            } else if (messageType == TRIAL_PERIOD_LIST) {
                return TRIAL_PERIOD_LIST;
            }else if (messageType == TRIAL_PERIOD) {
                return TRIAL_PERIOD;
            }
            throw new UnknownUpdateException("With update: " + update);
        }
        catch (Exception e){
            logger.warn(e.toString());
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
            return COMMAND_TEXT;
        }
    }

    /**
     * Метод обрабатывает сообщения и нажатия кнопок от пользователя с ролью USER
     * метод принимает параметрами функциональные интерфейсы.
     * В параметрах описаны действия, которые нужно применить к ключам этого метода.
     *
     * @param whatIsMenu     функция для попадания в нужную ветку условий
     * @param doSendMessage  биконсьюмер для отправки текста
     * @param doSendPhoto    консьюмер для отправки фото
     * @param doSendLocation биконсьюмер для отправки локации
     * @see pro.sky.telegrambot.listener.TelegramBotUpdatesListener
     */
    public void handleUserMessages(Function<String, Boolean> whatIsMenu,
                                   BiConsumer<String, String> doSendMessage,
                                   TriConsumer<File, String, String> doSendPhoto,
                                   BiConsumer<Float, Float> doSendLocation,
                                   User currentUser, Update update, ButtonsText buttonsText) throws IOException {
        if (whatIsMenu.apply("START_BUTTON")) {
            doSendMessage.accept("START_TEXT", "SPECIES_PET_SELECTION_MENU");
        } else if (whatIsMenu.apply("BACK_BUTTON")) {
            menuStackService.dropMenuStack(currentUser);
            String lastTextKey = menuStackService.getLastTextKeyByUser(currentUser);
            String lastMenuState = menuStackService.getLastMenuStateByUser(currentUser);
            menuStackService.dropMenuStack(currentUser);
            doSendMessage.accept(lastTextKey, lastMenuState);
        } else if (whatIsMenu.apply("CAT_BUTTON") || whatIsMenu.apply("DOG_BUTTON")) {
            menuStackService.setTextPackKey(currentUser, update.callbackQuery().data());
            buttonsText.changeCurrentTextKey(update.callbackQuery().data());
            doSendMessage.accept("GREETING_TEXT", "MAIN_MENU");
        } else if (whatIsMenu.apply("CHANGE_PET_BUTTON")) {
            doSendMessage.accept("START_TEXT", "SPECIES_PET_SELECTION_MENU");
        } else if (whatIsMenu.apply("INFO_BUTTON")) {
            doSendMessage.accept("INFO_TEXT", "INFO_MENU");
        } else if (whatIsMenu.apply("ABOUT_US_BUTTON")) {
            doSendMessage.accept("ABOUT_US", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("CONTACTS_BUTTON")) {
            doSendLocation.accept(51.165973F, 71.403983F);
            doSendPhoto.accept(address, "SHELTER_CONTACTS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("SAFETY_REGULATIONS_BUTTON")) {
            doSendMessage.accept("SAFETY_REGULATIONS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("SHARE_CONTACT_BUTTON")) {
            doSendMessage.accept("SHARE_CONTACT", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("HOW_TO_GET_PET_BUTTON")) {
            doSendMessage.accept("CONSULT_MENU_MESSAGE", "HOW_TO_GET_PET_MENU");
        } else if (whatIsMenu.apply("MEETING_WITH_PET_BUTTON")) {
            doSendMessage.accept("MEETING_WITH_PET", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("LIST_OF_DOCUMENTS_BUTTON")) {
            doSendMessage.accept("LIST_OF_DOCUMENTS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("HOW_TO_CARRY_ANIMAL_BUTTON")) {
            doSendMessage.accept("HOW_TO_CARRY_ANIMAL", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("MAKING_HOUSE_BUTTON")) {
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAKING_HOUSE_MENU");
        } else if (whatIsMenu.apply("FOR_PUPPY_BUTTON")) {
            doSendMessage.accept("MAKING_HOUSE_FOR_PUPPY", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("FOR_PET_BUTTON")) {
            doSendMessage.accept("MAKING_HOUSE_FOR_PET", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("FOR_PET_WITH_DISABILITIES_BUTTON")) {
            doSendMessage.accept("MAKING_HOUSE_FOR_PET_WITH_DISABILITIES", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("DOG_HANDLER_ADVICES_BUTTON")) {
            doSendMessage.accept("DOG_HANDLER_ADVICES", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("DOG_HANDLERS_BUTTON")) {
            doSendMessage.accept("DOG_HANDLERS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("DENY_LIST_BUTTON")) {
            doSendMessage.accept("DENY_LIST", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("CALL_VOLUNTEER_BUTTON")) {
            telegramBot.execute(menuService.sendTextLoader(
                    channelId,
                    buttonsText.getString("VOLUNTEER_REQUEST_TEXT"),
                    buttonsText.getMenu("TO_SUPPORT_ACCEPT_MENU"),
                    List.of(buttonsText.getString("BEGIN_PREFIX") + currentUser.getChatId().toString())));
            doSendMessage.accept("CALL_VOLUNTEER_TEXT", "BACK_TO_ONLY_MAIN_MENU");
        } else if (whatIsMenu.apply("BACK_TO_MAIN_MENU_BUTTON")) {
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAIN_MENU");
        } else {
            if (currentUser.getRole() == VOLUNTEER) {
                doSendMessage.accept("SOMETHING_WENT_WRONG", "VOLUNTEER_MAIN_MENU");
            }
            else if (currentUser.getRole() == ADMIN) {
                doSendMessage.accept("SOMETHING_WENT_WRONG", "ADMIN_MAIN_MENU");
            } else {
                doSendMessage.accept("ERROR_COMMAND_TEXT", "CALL_VOLUNTEER_MENU");
            }
        }
    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью PARENT
     *
     * @param update обновление
     */
    public void handleParentMessages(Function<String, Boolean> whatIsMenu,
                                     BiConsumer<String, String> doSendMessage,
                                     TriConsumer<File, String, String> doSendPhoto,
                                     BiConsumer<Float, Float> doSendLocation,
                                     User currentUser, Update update,
                                     ButtonsText buttonsText) throws IOException {

        if (whatIsMenu.apply("START_BUTTON")) {
            doSendMessage.accept("START_TEXT", "SPECIES_PET_SELECTION_MENU");
        } else if (whatIsMenu.apply("BACK_BUTTON")) {
            menuStackService.dropMenuStack(currentUser);
            String lastTextKey = menuStackService.getLastTextKeyByUser(currentUser);
            String lastMenuState = menuStackService.getLastMenuStateByUser(currentUser);
            menuStackService.dropMenuStack(currentUser);
            doSendMessage.accept(lastTextKey, lastMenuState);
        } else if (whatIsMenu.apply("CAT_BUTTON") || whatIsMenu.apply("DOG_BUTTON")) {
            menuStackService.setTextPackKey(currentUser, update.callbackQuery().data());
            buttonsText.changeCurrentTextKey(update.callbackQuery().data());
            doSendMessage.accept("GREETING_TEXT", "MAIN_MENU");
        } else if (whatIsMenu.apply("CHANGE_PET_BUTTON")) {
            doSendMessage.accept("START_TEXT", "SPECIES_PET_SELECTION_MENU");
        } else if (whatIsMenu.apply("INFO_BUTTON")) {
            doSendMessage.accept("INFO_TEXT", "INFO_MENU");
        } else if (whatIsMenu.apply("ABOUT_US_BUTTON")) {
            doSendMessage.accept("ABOUT_US", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("CONTACTS_BUTTON")) {
            doSendLocation.accept(51.165973F, 71.403983F);
            doSendPhoto.accept(address, "SHELTER_CONTACTS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("SAFETY_REGULATIONS_BUTTON")) {
            doSendMessage.accept("SAFETY_REGULATIONS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("SHARE_CONTACT_BUTTON")) {
            doSendMessage.accept("SHARE_CONTACT", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("HOW_TO_GET_PET_BUTTON")) {
            doSendMessage.accept("CONSULT_MENU_MESSAGE", "HOW_TO_GET_PET_MENU");
        } else if (whatIsMenu.apply("MEETING_WITH_PET_BUTTON")) {
            doSendMessage.accept("MEETING_WITH_PET", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("LIST_OF_DOCUMENTS_BUTTON")) {
            doSendMessage.accept("LIST_OF_DOCUMENTS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("HOW_TO_CARRY_ANIMAL_BUTTON")) {
            doSendMessage.accept("HOW_TO_CARRY_ANIMAL", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("MAKING_HOUSE_BUTTON")) {
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAKING_HOUSE_MENU");
        } else if (whatIsMenu.apply("FOR_PUPPY_BUTTON")) {
            doSendMessage.accept("MAKING_HOUSE_FOR_PUPPY", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("FOR_PET_BUTTON")) {
            doSendMessage.accept("MAKING_HOUSE_FOR_PET", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("FOR_PET_WITH_DISABILITIES_BUTTON")) {
            doSendMessage.accept("MAKING_HOUSE_FOR_PET_WITH_DISABILITIES", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("DOG_HANDLER_ADVICES_BUTTON")) {
            doSendMessage.accept("DOG_HANDLER_ADVICES", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("DOG_HANDLERS_BUTTON")) {
            doSendMessage.accept("DOG_HANDLERS", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("DENY_LIST_BUTTON")) {
            doSendMessage.accept("DENY_LIST", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("CALL_VOLUNTEER_BUTTON")) {
            telegramBot.execute(menuService.sendTextLoader(
                    channelId,
                    buttonsText.getString("VOLUNTEER_REQUEST_TEXT"),
                    buttonsText.getMenu("TO_SUPPORT_ACCEPT_MENU"),
                    List.of(buttonsText.getString("BEGIN_PREFIX") + currentUser.getChatId().toString())));
            doSendMessage.accept("CALL_VOLUNTEER_TEXT", "BACK_TO_ONLY_MAIN_MENU");
        } else if (whatIsMenu.apply("SEND_REPORT_BUTTON")) {
            doSendMessage.accept("SEND_REPORT_TEXT", "BACK_TO_MAIN_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, REPORT);
        } else if (menuStackService.getCurrentExpectedMessageTypeByUser(currentUser).equals(REPORT_TEXT)) {
            reportService.saveReport(currentUser.getChatId(), update.message().text());
            telegramBot.execute(new SendMessage(currentUser.getChatId(), reportService.checkNewReportByUser(currentUser.getChatId())));
            doSendMessage.accept("WHAT_NEXT_TEXT", "IN_REPORT_SEND_TEXT_MENU");
        } else if (menuStackService.getCurrentExpectedMessageTypeByUser(currentUser).equals(REPORT_PIC)) {
            reportService.getPictureFromMessage(currentUser.getChatId(), update.message());
            telegramBot.execute(new SendMessage(currentUser.getChatId(), reportService.checkNewReportByUser(currentUser.getChatId())));
            doSendMessage.accept("WHAT_NEXT_TEXT", "IN_REPORT_SEND_PIC_MENU");
        } else if (whatIsMenu.apply("FINISH_SENDING_REPORT")) {
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND_CALL_BACK);
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAIN_MENU");
        } else if (whatIsMenu.apply("MY_REPORTS")) {
            doSendMessage.accept("WHAT_NEXT_TEXT", "MY_REPORTS_MENU");
        } else if (whatIsMenu.apply("ALL_REPORTS")) {
            doSendParentReportList.accept("ALL_REPORTS", "ALL");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, MY_REPORTS);
        } else if (whatIsMenu.apply("UPDATE_REQUESTED")) {
            doSendParentReportList.accept("UPDATE_REQUESTED", "TO_BE_UPDATED");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, MY_REPORTS);
        } else if (whatIsMenu.apply("MY_TRIAL_PERIOD")) {
            doSendCustomMessage.accept(trialPeriodService.getTrialPeriodInformation(currentUser.getChatId()), "MAIN_MENU");
        } else if (whatIsMenu.apply("BACK_TO_MAIN_MENU_BUTTON")) {
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND_CALL_BACK);
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAIN_MENU");
        } else {
            doSendMessage.accept("ERROR_COMMAND_TEXT", "CALL_VOLUNTEER_MENU");
        }
    }



    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью VOLUNTEER
     *
     * @param whatIsMenu    функция для попадания в нужную ветку условий
     * @param doSendMessage биконсьюмер для отправки текста
     */
    public void handleVolunteerMessages(Function<String, Boolean> whatIsMenu,
                                        BiConsumer<String, String> doSendMessage,
                                        BiConsumer<String, String> doSendUsersList,
                                        BiConsumer<String, String> doSendReportList,
                                        BiConsumer<String, String> doSendReport,
                                        User currentUser, Update update) {


            if (whatIsMenu.apply("START_BUTTON")) {
                doSendMessage.accept("VOLUNTEER_START_TEXT", "VOLUNTEER_MAIN_MENU");
            } else if (menuStackService.getCurrentExpectedMessageTypeByUser(currentUser).equals(USER_NAME)) {
                doSendUsersList.accept("CHOOSE_USER_TO_MAKE_PARENT", "BACK_TO_VOLUNTEERS_MENU");
                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, ADDING_PARENT);
            } else if  (whatIsMenu.apply("ADD_PARENT_BUTTON_VOLUNTEER")) {
                doSendMessage.accept("ADD_PARENT_TEXT", "BACK_TO_VOLUNTEERS_MENU");
                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, USER_NAME);
            }
            else if (whatIsMenu.apply("CHECK_REPORTS_BUTTON")) {
                doSendReportList.accept("CHECK_REPORTS", "REPORTS_MENU");
            }
            else if (whatIsMenu.apply("UNREAD_REPORTS")) {
                 doSendReportList.accept("UNREAD_REPORTS_TEXT", "BACK_TO_VOLUNTEERS_MENU");
             } else if (whatIsMenu.apply("BACK_TO_REPORT_LIST")) {
                 doSendReportList.accept("UNREAD_REPORTS_TEXT", "BACK_TO_VOLUNTEERS_MENU");
             }
            else if (whatIsMenu.apply("VOLUNTEER_MAIN_MENU_BUTTON")) {
                doSendMessage.accept("VOLUNTEER_START_TEXT", "VOLUNTEER_MAIN_MENU");
                 menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
            } else if (whatIsMenu.apply("TRIAL_PERIODS")) {
                doSendTrialPeriodsList.accept("VOLUNTEER_START_TEXT", "VOLUNTEER_MAIN_MENU");
            } else if (menuStackService.getCurrentExpectedMessageTypeByUser(currentUser).equals(ADDING_PARENT)) {
                User newParent = userService.getUser(Long.valueOf(update.callbackQuery().data()));
                administrativeService.setParent(currentUser.getChatId(), newParent.getChatId());
                telegramBot.execute(new SendMessage(newParent.getChatId(), "Поздравляем, вы взяли питомца. Ваш испытательный период начался!"));
                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                doSendMessage.accept("AFTER_ADDING_PARENT", "VOLUNTEER_MAIN_MENU");
            }
            else {
                doSendMessage.accept("ERROR_TEXT", "VOLUNTEER_MAIN_MENU");
            }

        }






    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью ADMIN
     *
     * @param whatIsMenu    функция для попадания в нужную ветку условий
     * @param doSendMessage биконсьюмер для отправки текста
     * @param goSendPhoto   консьюмер для отправки фото
     * @param goBack        для кнопки "назад"
     */
    public void handleAdminMessages(Function<String, Boolean> whatIsMenu,
                                    BiConsumer<String, String> doSendMessage,
                                    Consumer<File> goSendPhoto,
                                    Runnable goBack,
                                    BiConsumer<String, String> doSetNewVolunteer,
                                    BiConsumer<String, String> doSendReportList,
                                    BiConsumer<String, String> doSendReport,
                                    User currentUser, Update update) {
        if (whatIsMenu.apply("START_BUTTON")) {
            doSendMessage.accept("ADMIN_START_TEXT", "ADMIN_MAIN_MENU");
        } else if (menuStackService.getCurrentExpectedMessageTypeByUser(currentUser).equals(USER_NAME)) {
            doSendUsersList.accept("CHOOSE_USER_TO_MAKE_PARENT", "BACK_TO_ADMIN_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, ADDING_PARENT);
        } else if  (whatIsMenu.apply("ADD_PARENT_BUTTON_VOLUNTEER")) {
            doSendMessage.accept("ADD_PARENT_TEXT", "BACK_TO_ADMIN_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, USER_NAME);
        }
        else if (whatIsMenu.apply("CHECK_REPORTS_BUTTON")) {
            doSendReportList.accept("CHECK_REPORTS", "REPORTS_MENU");
        }
        else if (whatIsMenu.apply("UNREAD_REPORTS")) {
            doSendReportList.accept("UNREAD_REPORTS_TEXT", "BACK_TO_ADMIN_MENU");
        } else if (whatIsMenu.apply("BACK_TO_REPORT_LIST")) {
            doSendReportList.accept("UNREAD_REPORTS_TEXT", "BACK_TO_ADMIN_MENU");
        } else if (whatIsMenu.apply("ADMIN_MAIN_MENU_BUTTON")) {
            doSendMessage.accept("ADMIN_START_TEXT", "ADMIN_MAIN_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
        }
        else {
            doSendMessage.accept("ERROR_TEXT", "ADMIN_MAIN_MENU");
        }
    }

    /**
     * Метод проверяет колбек:
     * - возвращает "0" если начинается с "pic" - значит нажата кнопка, связанная с фото;
     *
     * - возвращает "1" если начинается с "txt" - значит нажата кнопка, связанная с текстом;
     *
     * - возвращает "2" если начинается с "oke" - значит нажата кнопка - одобрено;
     *
     * - возвращает "3" если начинается с "acc" - accept Trial Period;
     *
     * - возвращает "4" если начинается с "pro" - prolong Trial Period;
     *
     * - возвращает "5" если начинается с "dec" - decline Trial Period;
     *
     * - иначе возвращается "-1"
     * @param update апдейт от бота
     * @return число, соответствующее колбеку
     */
    private int callBackChecker(Update update) {
        int status = -1;
        String data = update.callbackQuery().data();

        if (data.startsWith("pic")) {
            status = 0;
        } else if (data.startsWith("txt")) {
            status = 1;
        } else if (data.startsWith("oke")) {
            status = 2;
        } else if (data.startsWith("acc")) {
            status = 3;
        } else if (data.startsWith("pro")) {
            status = 4;
        }else if (data.startsWith("dec")) {
            status = 5;
        }


        return status;
    }

    private Long getIdFromCallback(Update update) {

        String data = update.callbackQuery().data();

        return Long.valueOf(data.substring(4));

    }

    /**
     * Метод проверяет, если была отправлена текстовая команда /start
     * Возвращает true если да.
     * @param update апдейт
     * @return Возвращает true если была отправлена текстовая команда /start
     */
    private boolean checkStartCommand(Update update) {
        if (update.message() != null && update.message().text() != null) {
            return update.message().text().startsWith("/start");
        }
        return false;
    }
}
