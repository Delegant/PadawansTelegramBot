package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.MenuService;
import pro.sky.telegrambot.service.ReportService;
import pro.sky.telegrambot.service.impl.ReportServiceImpl;
import pro.sky.telegrambot.service.impl.UserRepoService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static pro.sky.telegrambot.constants.ButtonsText.*;
import static pro.sky.telegrambot.constants.ResponsesText.*;
import static pro.sky.telegrambot.model.User.Role;

/**
 * Основной класс бота, где происходит обработка входящих обновлений из клиента
 * Имплементирует UpdatesListener в качестве обработчика обновлений
 *
 * @see com.pengrad.telegrambot.UpdatesListener
 */
@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    /**
     * ResourceBundle используется для хранения текстов, которые отправляет бот в ответ на запрос пользователя
     */
    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");
    /**
     * Инжекстированный сервис-класс, отвечающий за обработку и создание клавиатур
     *
     * @see MenuService
     */
    private final MenuService menuService;
    /**
     * Сервис репозитория, отвечащий за сохранение пользователей в БД
     *
     * @see UserRepoService
     */
    private final UserRepoService userService;
    /**
     * директория с файлом - схемой проезда к приюту
     */
    java.io.File address = new File("src/main/resources/MapPhoto/address.png");
    /**
     * Логгер для класса
     */
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    /**
     * поле инжектирует Телеграм-бота
     *
     * @see pro.sky.telegrambot.configuration.TelegramBotConfiguration
     */
    private TelegramBot telegramBot;

    private ReportService reportService;

    /**
     * конструктор класса
     *
     * @param telegramBot Телеграм бот
     * @param menuService обработчик-меню
     * @param userService сервис репозитория пользоваьелей
     */
    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      MenuService menuService,
                                      UserRepoService userService,
                                      ReportService reportService) {
        this.telegramBot = telegramBot;
        this.menuService = menuService;
        this.userService = userService;
        this.reportService = reportService;
    }

    /**
     * Метод, запускающий (инициализирующий обработчик обновлений)
     */
    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    /**
     * Метод, проверяющий наличие пользователя в базе и возвращающий роль пользователя
     * для дальнейшей работы. Если пользователя в базе нет - сохраняет его и возвращает роль по-умолчанию - USER
     *
     * @param message ообщение из обновления
     * @return роль пользователя (USER.ROLE {USER, PARENT, VOLUNTEER, ADMIN})
     * @see User
     */
    private User.Role checkUser(Message message) {
        Long chatId = message.chat().id();
        String lastName = message.chat().lastName();
        String firstName = message.chat().firstName();
        User currentUser = userService.getUserByChatId(chatId).orElseGet(() -> userService.createUser(chatId, lastName + " " + firstName));
        return currentUser.getRole();
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
            logger.info("Processing update: {}", update);
//            Message message = null;
//            if (update.message() == null) {
//                message = update.callbackQuery().message();
//            } else if (update.message().photo() != null) {
//                reportService.getPictureFromMessage(update.message().from().id(), update.message());
//            } else {
//                message = update.message();
//            }
            Message message = (update.message() != null) ? update.message() : update.callbackQuery().message();
            try {
                if (update.callbackQuery() != null && update.message() != null) {

                    Function<String, Boolean> whatIsMenu = (someButtonName) -> {
                        String hashFromButton = menuService.getHashFromButton(someButtonName);
                        return update.callbackQuery().data().equals(hashFromButton);
                    };

                    BiConsumer<String, List<String>> doSendMessage = (text, menu) -> {
                        logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                        telegramBot.execute(menuService.editMenuLoader(update, text, menu));
                    };
                    Consumer<File> goSendPhoto = (filePath) -> {
                        logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                        telegramBot.execute(menuService.sendPhotoLoader(update, filePath));
                    };
                    BiConsumer<Float, Float> goSendLocation = (latitude, longitude) -> {
                        logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                        telegramBot.execute(menuService.sendLocationLoader(update, latitude, longitude));
                    };
                    if (update.message() != null && update.message().text() != null) {
                        whatIsMenu = (someButtonName) -> message.text().equals(someButtonName);
                        doSendMessage = (text, menu) -> {
                            logger.info("==== Processing update with message: {}", message.text());
                            telegramBot.execute(menuService.menuLoader(message, text, menu));
                        };
                    }
                    else if (update.message() == null && update.callbackQuery() == null) {
                        logger.warn("==== Update didn't have message or callBack: {}", update);
                        whatIsMenu = EXCEPTION::equals;
                    }

                    if (checkUser(message).equals(Role.USER) || checkUser(message).equals(Role.PARENT)) {
                        handleUserMessages(whatIsMenu, doSendMessage, goSendPhoto, goSendLocation);
                    } else if (checkUser(message).equals(Role.VOLUNTEER)) {
                        handleVolunteerMessages(whatIsMenu, doSendMessage, goSendPhoto);
                    } else if (checkUser(message).equals(Role.ADMIN)) {
                        reportService.getPictureFromMessage(update.message().from().id(), update.message());
                }
            } else if (update.message().photo() != null) {
                    reportService.getPictureFromMessage(update.message().from().id(), update.message());
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью USER
     *
     * @param whatIsMenu     функция для попадания в нужную ветку условий
     * @param doSendMessage  биконсьюмер для отправки текста
     * @param goSendPhoto    консьюмер для отправки фото
     * @param goSendLocation биконсьюмер для отправки локации
     */
    public void handleUserMessages(Function<String, Boolean> whatIsMenu,
                                   BiConsumer<String, List<String>> doSendMessage,
                                   Consumer<File> goSendPhoto,
                                   BiConsumer<Float, Float> goSendLocation
    ) {
        if (whatIsMenu.apply(START)) {
            doSendMessage.accept(START_TEXT, MAIN_MENU);
        } else if (whatIsMenu.apply(INFO_BUTTON)) {
            doSendMessage.accept(INFO_TEXT, INFO_MENU);
        } else if (whatIsMenu.apply(ABOUT_US_BUTTON)) {
            doSendMessage.accept(ABOUT_US, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(SAFETY_REGULATIONS_BUTTON)) {
            doSendMessage.accept(SAFETY_REGULATIONS, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(CONTACTS_BUTTON)) {
            goSendLocation.accept(51.165973F, 71.403983F);
            goSendPhoto.accept(address);
            doSendMessage.accept(SHELTER_CONTACTS, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(SHARE_CONTACT_BUTTON)) {
            doSendMessage.accept(SHARE_CONTACT, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(HOW_TO_GET_DOG_BUTTON)) {
            doSendMessage.accept(CONSULT_MENU_MESSAGE, HOW_TO_GET_DOG_MENU);
        } else if (whatIsMenu.apply(MEETING_WITH_DOG_BUTTON)) {
            doSendMessage.accept(MEETING_WITH_DOG, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(LIST_OF_DOCUMENTS_BUTTON)) {
            doSendMessage.accept(LIST_OF_DOCUMENTS, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(HOW_TO_CARRY_ANIMAL_BUTTON)) {
            doSendMessage.accept(HOW_TO_CARRY_ANIMAL, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(MAKING_HOUSE_BUTTON)) {
            doSendMessage.accept(DEFAULT_MENU_TEXT, MAKING_HOUSE_MENU);
        } else if (whatIsMenu.apply(FOR_PUPPY_BUTTON)) {
            doSendMessage.accept(MAKING_HOUSE_FOR_PUPPY, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(FOR_DOG_BUTTON)) {
            doSendMessage.accept(MAKING_HOUSE_FOR_DOG, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(FOR_DOG_WITH_DISABILITIES_BUTTON)) {
            doSendMessage.accept(MAKING_HOUSE_FOR_DOG_WITH_DISABILITIES, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(DOG_HANDLER_ADVICES_BUTTON)) {
            doSendMessage.accept(DOG_HANDLER_ADVICES, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(DOG_HANDLERS_BUTTON)) {
            doSendMessage.accept(DOG_HANDLERS, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(DENY_LIST_BUTTON)) {
            doSendMessage.accept(DENY_LIST, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(BACK_TO_MAIN_MENU_BUTTON)) {
            doSendMessage.accept(DEFAULT_MENU_TEXT, MAIN_MENU);
        } else {
            doSendMessage.accept(ERROR_COMMAND_TEXT, MAIN_MENU);
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
     * @param goSendPhoto   консьюмер для отправки фото
     */
    public void handleVolunteerMessages(Function<String, Boolean> whatIsMenu,
                                        BiConsumer<String, List<String>> doSendMessage,
                                        Consumer<File> goSendPhoto) {
        if (whatIsMenu.apply(ADD_PARENT_BUTTON)) {
            doSendMessage.accept(ADD_PARENT, BACK_TO_VOLUNTEERS_MENU);
        } else if (whatIsMenu.apply(CHECK_REPORTS_BUTTON)) {
            doSendMessage.accept(CHECK_REPORTS, BACK_TO_VOLUNTEERS_MENU);
        }

    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью ADMIN
     *
     * @param update обновление
     */
    public void handleAdminMessages(Update update) {

    }

}
