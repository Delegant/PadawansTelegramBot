package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.constants.ButtonsText;
import pro.sky.telegrambot.exceptions.UnknownUpdateException;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.AdministrativeService;
import pro.sky.telegrambot.service.MenuService;
import pro.sky.telegrambot.service.MenuStackService;
import pro.sky.telegrambot.service.ReportService;
import pro.sky.telegrambot.service.impl.UserServiceImpl;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
     * Инжекстированный сервис-класс, отвечающий за обработку и создание клавиатур
     *
     * @see MenuService
     */
    private final MenuService menuService;
    /**
     * Сервис репозитория, отвечащий за сохранение пользователей в БД
     *
     * @see UserServiceImpl
     */
    private final UserServiceImpl userService;
    /**
     * Инжекстированный сервис-класс, отвечающий за отслеживанием положения
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

    private final AdministrativeService administrativeService;
    /**
     * директория с файлом - схемой проезда к приюту
     */
    java.io.File address = new File("src/main/resources/MapPhoto/address.png");
    Function<String, Boolean> whatIsMenu;
    BiConsumer<String, String> doSendMessage;
    Consumer<File> doSendPhoto;
    BiConsumer<Float, Float> goSendLocation;
    BiConsumer<String, String> doSendReportList;

    BiConsumer<String, String> doSendUsersList;
    Consumer<File> goSendPhoto;
    Runnable goBack;
    BiConsumer<String, String> doSetNewVolunteer;

    /**
     * конструктор класса
     *
     * @param telegramBot      Телеграм бот
     * @param menuService      обработчик-меню
     * @param userService      сервис репозитория пользоваьелей
     * @param menuStackService сервис репозитория положения юезера в меню
     */
    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      MenuService menuService,
                                      UserServiceImpl userService,
                                      MenuStackService menuStackService,
                                      ReportService reportService,
                                      AdministrativeService administrativeService) {
        this.telegramBot = telegramBot;
        this.menuService = menuService;
        this.userService = userService;
        this.reportService = reportService;
        this.menuStackService = menuStackService;
        this.administrativeService = administrativeService;
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
            logger.info("========Log start point from process() update: {}", update);
            Message message = (update.message() != null) ? update.message() : update.callbackQuery().message();
            User currentUser = userService.getUserByMessage(message);
            Role roleCurrentUser = currentUser.getRole();
            String textPackKey = menuStackService.getLastTextPackKeyByUser(currentUser);
            MessageType expectedTypeCurrentMessage = menuStackService.getCurrentExpectedMessageTypeByUser(currentUser);
            MessageType realTypeCurrentMessage = getCurrentMessageType(expectedTypeCurrentMessage, update, message);
            //           menuStackService.createMenuStack(currentUser, textPackKey, expectedTypeCurrentMessage);
            ButtonsText buttonsText = ButtonsText.getButtonText(textPackKey);
            logger.info("====Received message is processed as {} and from {}, log from process()", realTypeCurrentMessage, roleCurrentUser);
            try {
                if (realTypeCurrentMessage == COMMAND_CALL_BACK || realTypeCurrentMessage == COMMAND_TEXT) {
                    if (realTypeCurrentMessage == COMMAND_CALL_BACK) {
                        functionalInitForCallBack(update, buttonsText, currentUser);
                    } else {
                        functionalInitForTextCommand(message, buttonsText);
                    }
                    if (roleCurrentUser == USER || roleCurrentUser == PARENT) {
                        handleUserMessages(whatIsMenu, doSendMessage, doSendPhoto, goSendLocation, currentUser, update, buttonsText);
                    } else if (roleCurrentUser == VOLUNTEER) {
                        handleVolunteerMessages(whatIsMenu, doSendMessage, doSendUsersList, currentUser, update);
                    } else if (roleCurrentUser == ADMIN) {
                        handleAdminMessages(whatIsMenu, doSendMessage, goSendPhoto, doSetNewVolunteer, doSendUsersList, currentUser, update);

                    }
                } else if (realTypeCurrentMessage == REPORT_PIC) {
                    if (roleCurrentUser == PARENT) {
                        reportService.getPictureFromMessage(message.from().id(), message);
                    }
                } else if (realTypeCurrentMessage == REPORT_TEXT) {
                    if (roleCurrentUser == PARENT) {
                        //todo добавить метод записи текста
                    }
                } else if (realTypeCurrentMessage == DIALOG_TEXT) {
                    telegramBot.execute(menuService.sendTextLoader(currentUser.getCompanion(), message.text()));
                    if (roleCurrentUser == VOLUNTEER) {
                        if (realTypeCurrentMessage == ADDING_PARENT) {

                        }
                        //todo добавить меню завершения диалога
                    }
                } else if (realTypeCurrentMessage == DIALOG_REQUEST) {
                    if (roleCurrentUser == USER || roleCurrentUser == PARENT) {
                        //добавить метод записи текста
                        //поиск всех волонтеров
                        Collection<User> allVolunteer = userService.usersWithEqualRole(VOLUNTEER);
                        allVolunteer.forEach((volunteer) -> telegramBot.execute(menuService.sendTextLoader(volunteer.getChatId(), message.text(), buttonsText.getMenu("DENY_DIALOG")))
                        );
                        //отправка волонтерам меню с подтверждением
                    } else if (roleCurrentUser == VOLUNTEER) {
                        //todo добавить меню завершения диaлога
                    }
                } else if (expectedTypeCurrentMessage.equals(USER_NAME)) {
                    functionalInitForCallBack(update, buttonsText, currentUser);
                    handleVolunteerMessages(whatIsMenu, doSendMessage, doSendUsersList, currentUser, update);
                } else if (expectedTypeCurrentMessage.equals(ADDING_PARENT)) {
                    functionalInitForCallBack(update, buttonsText, currentUser);
                    handleVolunteerMessages(whatIsMenu, doSendMessage, doSendUsersList, currentUser, update);
                }
            } catch (Exception e) {
                logger.warn("====Exception: ", e);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    /**
     * Метод инициализирует функциональные интерфейсы в ситуации для update с текстом callBack.
     * Применяются интерфейсы, как действия по отношению к ключам в методе для пользователя:
     * {@link #handleUserMessages(Function, BiConsumer, Consumer, BiConsumer, User, Update, ButtonsText)}
     * и в методе для волонтера
     * {@link #handleVolunteerMessages(Function, BiConsumer, BiConsumer, User, Update)}
     */
    private void functionalInitForCallBack(Update update, ButtonsText buttonsText, User currentUser) {
        whatIsMenu = (someButtonNameKey) -> {
            String someButtonNameValue = buttonsText.getString(someButtonNameKey);
            String hashSomeButton = menuService.getHashFromButton(someButtonNameValue);
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
        doSendPhoto = (filePath) -> {
            logger.info("====Processing doSendPhoto with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.sendPhotoLoader(update, filePath));
        };
        goSendLocation = (latitude, longitude) -> {
            logger.info("====Processing goSendLocation with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.sendLocationLoader(update, latitude, longitude));
        };
        doSendReportList = (textKey, menuKey) -> {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            List<String> buttons = menuService.generateListOfLastReports();
            String text = "Список последних отчетов: ";
            telegramBot.execute(menuService.menuLoader(update, text, buttons));
        };
        doSendUsersList = (textKey, menuKey) -> {
            logger.info("==== Getting list of users with name like {}", update.message().text());
            List<List<String>> listOfUsers = menuService.generateListOfUsers(update.message().text());
            telegramBot.execute(menuService.menuLoaderForObjects(update.message(), "Выберите пользователя для назначения усыновителем", listOfUsers));
        };
    }

    /**
     * Метод инициализирует функциональные интерфейсы в ситуации для update с текстовой командой.
     * Применяются интерфейсы как действия по отношению к ключам в методе для пользователя:
     * {@link #handleUserMessages(Function, BiConsumer, Consumer, BiConsumer, User, Update, ButtonsText)}
     * и в методе для волонтера {@link #handleVolunteerMessages(Function, BiConsumer, BiConsumer, User, Update)}
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
     *
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
            } else if (messageType == COMMAND && update.callbackQuery().message() != null) {
                return COMMAND_CALL_BACK;
            } else if (messageType == REPORT && update.message() != null) {
                return REPORT_TEXT;
            } else if (messageType == REPORT && message.photo() != null) {
                return REPORT_PIC;
            } else if (messageType == DIALOG && update.message() != null && update.message().text().startsWith("/")) {
                return DIALOG_COMMAND;
            } else if (messageType == DIALOG_REQUEST && update.message() != null) {
                return DIALOG_REQUEST;
            } else if (messageType == DIALOG && update.message() != null) {
                return DIALOG_TEXT;
            } else if (messageType == DIALOG && message.photo() != null) {
                return DIALOG_PIC;
            } else if (messageType == REPORT_REQUEST && update.callbackQuery() != null) {
                return REPORT_LIST;
            } else if (messageType == ADDING_PARENT && update.message() != null) {
                return ADDING_PARENT;
            }
            throw new UnknownUpdateException("With update: " + update);
        } catch (Exception e) {
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
                                   Consumer<File> doSendPhoto,
                                   BiConsumer<Float, Float> doSendLocation,
                                   User currentUser, Update update, ButtonsText buttonsText) {
        if (whatIsMenu.apply("START_BUTTON")) {
            doSendMessage.accept("START_TEXT", "SPECIES_PET_SELECTION_MENU");
        } else if (whatIsMenu.apply("BACK_BUTTON")) {
            menuStackService.dropMenuStack(currentUser);
            doSendMessage.accept(menuStackService.getLastTextKeyByUser(currentUser),
                    menuStackService.getLastMenuStateByUser(currentUser));
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
            doSendPhoto.accept(address);
            doSendMessage.accept("SHELTER_CONTACTS", "BACK_TO_MAIN_MENU");
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
//            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, DIALOG);
            Collection<User> allVolunteer = userService.usersWithEqualRole(VOLUNTEER);
            allVolunteer.forEach((volunteer) -> {
                        telegramBot.execute(menuService.sendTextLoader(volunteer.getChatId(),
                                buttonsText.getString("VOLUNTEER_REQUEST_TEXT"),
                                buttonsText.getMenu("TO_SUPPORT_MENU"), List.of(currentUser.getChatId().toString())));
                    }

            );
            menuStackService.setExpectedMessageTypeByRole(VOLUNTEER, DIALOG_REQUEST);
//          тут точка входа в диалог
//          должна быть коллективная рассылка запроса
//            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, DIALOG_REQUEST);
            doSendMessage.accept("CALL_VOLUNTEER_TEXT", "BACK_TO_MAIN_MENU");
        } else if (whatIsMenu.apply("BACK_TO_MAIN_MENU_BUTTON")) {
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAIN_MENU");
        } else {
            doSendMessage.accept("ERROR_COMMAND_TEXT", "CALL_VOLUNTEER_MENU");
        }
    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью PARENT
     *
     * @param update обновление
     */
    public void handleParentMessages(Update update) {

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
                                        User currentUser, Update update) {

        if (whatIsMenu.apply("START_BUTTON")) {
            doSendMessage.accept("VOLUNTEER_START_TEXT", "VOLUNTEER_MAIN_MENU");
        } else if (menuStackService.getCurrentExpectedMessageTypeByUser(currentUser).equals(USER_NAME)) {
            doSendUsersList.accept("CHOOSE_USER_TO_MAKE_PARENT", "BACK_TO_VOLUNTEERS_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, ADDING_PARENT);
        } else if (whatIsMenu.apply("ADD_PARENT_BUTTON_VOLUNTEER")) {
            doSendMessage.accept("ADD_PARENT_TEXT", "BACK_TO_VOLUNTEERS_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, USER_NAME);
        } else if (whatIsMenu.apply("CHECK_REPORTS_BUTTON")) {
            doSendMessage.accept("CHECK_REPORTS", "BACK_TO_VOLUNTEERS_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, USER_NAME);
        } else if (whatIsMenu.apply("ACCEPT_DIALOG")) {
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, DIALOG_REQUEST);
        } else if (whatIsMenu.apply("VOLUNTEER_MAIN_MENU_BUTTON")) {
            doSendMessage.accept("VOLUNTEER_START_TEXT", "VOLUNTEER_MAIN_MENU");
        } else if (menuStackService.getCurrentExpectedMessageTypeByUser(currentUser).equals(ADDING_PARENT)) {
            User newParent = userService.getUser(Long.valueOf(update.callbackQuery().data()));
            administrativeService.setParent(currentUser.getChatId(), newParent.getChatId());
            telegramBot.execute(new SendMessage(newParent.getChatId(), "Поздравляем, вы взяли питомца. Ваш испытательный период начался!"));
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
            whatIsMenu.apply("VOLUNTEER_MAIN_MENU");
            doSendMessage.accept("DEFAULT_MENU_TEXT", "VOLUNTEER_MAIN_MENU");
        }
    }


    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью ADMIN
     *
     * @param whatIsMenu    функция для попадания в нужную ветку условий
     * @param doSendMessage биконсьюмер для отправки текста
     * @param goSendPhoto   консьюмер для отправки фото
     */
    public void handleAdminMessages(Function<String, Boolean> whatIsMenu,
                                    BiConsumer<String, String> doSendMessage,
                                    Consumer<File> goSendPhoto,
                                    BiConsumer<String, String> doSetNewVolunteer,
                                    BiConsumer<String, String> doSendReportList,
                                    User currentUser, Update update) {
        if (whatIsMenu.apply("START_BUTTON")) {
            doSendMessage.accept("ADMIN_START_TEXT", "ADMIN_MAIN_MENU");
        } else if (whatIsMenu.apply("ADD_PARENT_BUTTON")) {
            doSendMessage.accept("ADD_PARENT", "BACK_TO_ADMIN_MENU");
        } else if (whatIsMenu.apply("CHECK_REPORTS_BUTTON")) {
            doSendMessage.accept("CHECK_REPORTS", "REPORTS_MENU");
        } else if (whatIsMenu.apply("UNREAD_REPORTS")) {
            doSendReportList.accept("UNREAD_REPORTS_TEXT", "BACK_TO_ADMIN_MENU");
        }
    }
}
