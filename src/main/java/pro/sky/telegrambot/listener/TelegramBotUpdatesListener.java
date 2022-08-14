package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.component.MenuSelectorFactory;
import pro.sky.telegrambot.constants.ButtonsText;
import pro.sky.telegrambot.exceptions.UnknownUpdateException;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.TrialPeriod;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.*;
import pro.sky.telegrambot.service.impl.UserServiceImpl;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static pro.sky.telegrambot.model.MenuStack.MessageType;
import static pro.sky.telegrambot.model.MenuStack.MessageType.*;
import static pro.sky.telegrambot.model.User.Role;
import static pro.sky.telegrambot.model.User.Role.*;

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
    Function<String, Boolean> whatIsMenu;
    BiConsumer<String, String> doSendMessage;
    BiConsumer<String, String> doSendUsersList;
    BiConsumer<String, String> doSendReport;
    TriConsumer<Long, Long, String> doSendUserNotification;
    BiConsumer<Long, String> doSendTrialPeriod;
    MenuSelectorFactory menuSelectorFactory;

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
                                      TrialPeriodService trialPeriodService,
                                      MenuSelectorFactory menuSelectorFactory) {
        this.telegramBot = telegramBot;
        this.menuService = menuService;
        this.userService = userService;
        this.reportService = reportService;
        this.menuStackService = menuStackService;
        this.administrativeService = administrativeService;
        this.trialPeriodService = trialPeriodService;
        this.menuSelectorFactory = menuSelectorFactory;
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
                if (realTypeCurrentMessage == START_COMMAND) {
                    menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                    menuSelectorFactory.getMenuSelectorForTextCommand(roleCurrentUser, message, buttonsText).handleMessages();
                    }
                else if (expectedTypeCurrentMessage == COMMAND || expectedTypeCurrentMessage == COMMAND_CALL_BACK) {
                    menuSelectorFactory.getMenuSelectorForCallBack(roleCurrentUser, currentUser, update, buttonsText).handleMessages();
                }
                else if (realTypeCurrentMessage == REPORT_LIST) {
                        functionalInitForCallBack(update, buttonsText, currentUser);
                        if (roleCurrentUser == PARENT) {
                            doSendReport.accept(update.callbackQuery().data(), "INSIDE_PARENT_REPORT_MENU");
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, READING_REPORT);
                        }
                        doSendReport.accept(update.callbackQuery().data(), "INSIDE_REPORT_MENU");
                        menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, READING_REPORT);
                    }
                else if (realTypeCurrentMessage == REPORT_ACTION) {
                        functionalInitForCallBack(update, buttonsText, currentUser);
                        if (roleCurrentUser == PARENT) {
                            if (callBackChecker(update) == 0) {
                                currentUser.setTemp(getIdFromCallback(update).toString());
                                userService.updateUser(currentUser);
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, RECEIVED_REPORT_NOTIFICATION);
                                doSendMessage.accept("ASK_TO_SEND_PIC", "IN_REPORT_SEND_TEXT_MENU");
                            }
                            else if (callBackChecker(update) == 1) {
                                currentUser.setTemp(getIdFromCallback(update).toString());
                                userService.updateUser(currentUser);
                                menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, RECEIVED_REPORT_NOTIFICATION);
                                doSendMessage.accept("ASK_TO_SEND_TEXT", "IN_REPORT_SEND_TEXT_MENU");

                            }
                            else {
                                menuSelectorFactory.getMenuSelectorForCallBack(roleCurrentUser, currentUser, update, buttonsText).handleMessages();
                            }
                        }
                        else {
                            if (callBackChecker(update) == 0) {
                                Report report = reportService.getReportById(getIdFromCallback(update));
                                report.setReadStatus(Report.ReadStatus.TO_BE_UPDATED);
                                reportService.saveReport(report);
                                doSendUserNotification.accept(report.getUser().getChatId(), report.getId(), "Пришлите фотографию для отчета с номером " + report.getId());

                            }
                            else if (callBackChecker(update) == 1) {
                                Report report = reportService.getReportById(getIdFromCallback(update));
                                report.setReadStatus(Report.ReadStatus.TO_BE_UPDATED);
                                reportService.saveReport(report);
                                doSendUserNotification.accept(report.getUser().getChatId(), report.getId(), "Дополните текст отчета с номером " + report.getId());

                            }
                            else if (callBackChecker(update) == 2) {
                                Report report = reportService.getReportById(getIdFromCallback(update));
                                report.setReadStatus(Report.ReadStatus.READ);
                                reportService.saveReport(report);
                                telegramBot.execute(new SendMessage(report.getUser().getChatId(), "Отчет с номером  " + report.getId() + " принят волонтером " + currentUser.getName() + "."));
                            }
                            else {
                                menuSelectorFactory.getMenuSelectorForTextCommand(roleCurrentUser, message, buttonsText).handleMessages();
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
                        }
                        else if (update.message().text() != null) {
                            reportService.updateReport(currentUser.getChatId(), update.message().text());
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                            currentUser.setTemp(null);
                            userService.updateUser(currentUser);
                            doSendMessage.accept("SEND_REPORT_IS_CREATED", "MAIN_MENU");
                        }
                        else {
                            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                            menuSelectorFactory.getMenuSelectorForTextCommand(roleCurrentUser, message, buttonsText).handleMessages();
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
                else if (realTypeCurrentMessage == REPORT_PIC ) {
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
                else if (realTypeCurrentMessage == REPORT_TEXT) {
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
                            menuSelectorFactory.getMenuSelectorForTextCommand(roleCurrentUser, message, buttonsText).handleMessages();
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
                                menuSelectorFactory.getMenuSelectorForTextCommand(roleCurrentUser, message, buttonsText).handleMessages();
                            }
                        }
                else if (expectedTypeCurrentMessage == REPORT_TEXT) {
                    reportService.saveReport(currentUser.getChatId(), update.message().text());
                    telegramBot.execute(new SendMessage(currentUser.getChatId(), reportService.checkNewReportByUser(currentUser.getChatId())));
                    doSendMessage.accept("WHAT_NEXT_TEXT", "IN_REPORT_SEND_TEXT_MENU");
                    }
                else if (expectedTypeCurrentMessage == REPORT_PIC) {
                    try {
                        reportService.getPictureFromMessage(currentUser.getChatId(), update.message());
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                    telegramBot.execute(new SendMessage(currentUser.getChatId(), reportService.checkNewReportByUser(currentUser.getChatId())));
                    doSendMessage.accept("WHAT_NEXT_TEXT", "IN_REPORT_SEND_PIC_MENU");
                    }
                } catch (Exception e) {
                    logger.warn("====Exception: ", e);
                }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    /**
     * Метод инициализирует функциональные интерфейсы в ситуации для update с текстом callBack.
     * Применяются интерфейсы как действия по отношению к ключам в методе process()
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
        doSendUserNotification = (userId, reportId, text) -> {
            logger.info("==== Sending notification message to user");
            telegramBot.execute(menuService.sendReportNotificationMessage(userId, reportId, text));
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
            if (update.message() != null && update.message().text() != null && update.message().text().startsWith("/start")) {
                return START_COMMAND;
            } else if (messageType == COMMAND && update.message() != null) {
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
            } else if (messageType == ADDING_PARENT && update.callbackQuery() != null) {
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
}
