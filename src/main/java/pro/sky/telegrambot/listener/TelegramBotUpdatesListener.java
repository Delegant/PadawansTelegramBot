package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.constants.ResponsesText;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.MenuService;
import pro.sky.telegrambot.service.impl.UserRepoService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import static pro.sky.telegrambot.constants.ButtonsText.*;
import static pro.sky.telegrambot.constants.CaseText.*;
import static pro.sky.telegrambot.constants.ResponsesText.*;
import static pro.sky.telegrambot.constants.ResponsesText.ABOUT_US;
import static pro.sky.telegrambot.constants.ResponsesText.SAFETY_REGULATIONS;
import static pro.sky.telegrambot.constants.ResponsesText.SHARE_CONTACT;

/**
 * Основной класс бота, где происходит обработка входящих обновлений из клиента
 * Имплементирует UpdatesListener в качестве обработчика обновлений
 * @see com.pengrad.telegrambot.UpdatesListener
 */
@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    /**
     * Логгер для класса
     */
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    /**
     * ResourceBundle используется для хранения текстов, которые отправляет бот в ответ на запрос пользователя
     */
    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");

    /**
     * директория с файлом - схемой проезда к приюту
     */
    java.io.File address = new File("src/main/resources/MapPhoto/address.png");

    /**
     * поле инжектирует Телеграм-бота
     * @see pro.sky.telegrambot.configuration.TelegramBotConfiguration
     */
    private TelegramBot telegramBot;

    /**
     * Инжекстированный сервис-класс, отвечающий за обработку и создание клавиатур
     * @see MenuService
     */
    private final MenuService menuService;

    /**
     * Сервис репозитория, отвечащий за сохранение пользователей в БД
     * @see UserRepoService
     */
    private final UserRepoService repoService;

    /**
     * конструктор класса
     * @param telegramBot Телеграм бот
     * @param menuService обработчик-меню
     * @param repoService сервис репозитория пользоваьелей
     */
    public TelegramBotUpdatesListener(TelegramBot telegramBot, MenuService menuService, UserRepoService repoService) {
        this.telegramBot = telegramBot;
        this.menuService = menuService;
        this.repoService = repoService;
    }

    /**
     * Метод, запускающий (инициализирующий обработчик обновлений)
     */
    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    /**
     * Метод, проверяющий обновление на наличие Колбека
     * @param update обновление
     * @return возвращает true, если есть колбек
     */
    private boolean hasCallbackQuery(Update update) {
        return update.callbackQuery() != null;
    }

    /**
     * Метод, проверяющий наличие пользователя в базе и возвращающий роль пользователя
     * для дальнейшей работы. Если пользователя в базе нет - сохраняет его и возвращает роль по-умолчанию - USER
     * @param update обновление
     * @return роль пользователя (USER.ROLE {USER, PARENT, VOLUNTEER, ADMIN})
     * @see User
     */
    private User.Role checkUser(Update update) {
        User botUser = null;
        if (hasCallbackQuery(update)) {
            botUser = new User(update.callbackQuery().message().chat().id(),
                    update.callbackQuery().message().chat().lastName() + " " + update.callbackQuery().message().chat().firstName());
        } else {
            botUser = new User(update.message().chat().id(),
                    update.message().chat().lastName() + " " + update.message().chat().firstName());
        }
        if (repoService.getUserById(botUser.getChatId()).isEmpty()) {
            repoService.createUser(botUser.getChatId(), botUser.getName());
        }
        return botUser.getRole();
    }

    /**
     * Основной метод класса, в котором происходит обработка обновлений
     * @param updates Обновления, поступившие от бота
     * @return подтверждение обработки обновлений
     */
    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            Message message = update.message();
            try {
                if (!hasCallbackQuery(update)) {
                    if (message.text() == null) {
                        logger.info("!!!! file has been received by bot");
                    } else if (message.text().equals("/start")) {
                        telegramBot.execute(menuService.menuLoader(message, START_TEXT, MAIN_MENU));
                    }
                } else if(checkUser(update).equals(User.Role.USER)){
                    handleUserMessages(update);
                } else if (checkUser(update).equals(User.Role.ADMIN)) {
                    handleAdminMessages(update);
                }else if (checkUser(update).equals(User.Role.VOLUNTEER)) {
                    handleVolunteerMessages(update);
                }else if (checkUser(update).equals(User.Role.PARENT)) {
                    handleParentMessages(update);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью USER
     * @param update обновление
     */
    public void handleUserMessages(Update update) {
        String action = update.callbackQuery().data();
        switch (action) {
            case INFO_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(menuService.menuLoader(update, INFO_TEXT,  INFO_MENU));
                break;
            case ABOUT_US_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(menuService.menuLoader(update, ABOUT_US, BACK_TO_MAIN_MENU));
                break;
            case SAFETY_REGULATIONS_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(menuService.menuLoader(update, SAFETY_REGULATIONS, BACK_TO_MAIN_MENU));
                break;
            case CONTACTS_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(new SendLocation(update.callbackQuery().message().chat().id(),51.165973F, 71.403983F));
                telegramBot.execute(new SendPhoto(update.callbackQuery().message().chat().id(), address));
                telegramBot.execute(menuService.menuLoader(update, SHELTER_CONTACTS, BACK_TO_MAIN_MENU));
                break;
            case SHARE_CONTACTS_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(menuService.menuLoader(update, SHARE_CONTACT, BACK_TO_MAIN_MENU));
                break;
            case HOW_TO_GET_DOG_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(menuService.menuLoader(update, DEFAULT_MENU_TEXT, HOW_TO_GET_DOG_MENU));
                break;
            case PROCEDURE_MENU_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(menuService.menuLoader(update, DEFAULT_MENU_TEXT, PROCEDURE_MENU));
                break;
            case DOG_HANDLERS_MENU_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(menuService.menuLoader(update, DEFAULT_MENU_TEXT, DOG_HANDLERS_MENU));
                break;
            case MAKING_HOUSE_MENU_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(menuService.menuLoader(update, DEFAULT_MENU_TEXT, MAKING_HOUSE_MENU));
                break;
            case BACK_TO_MAIN_MENU_CASE:
                logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                telegramBot.execute(menuService.menuLoader(update, DEFAULT_MENU_TEXT, MAIN_MENU));
                break;
        }
    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью PARENT
     * @param update обновление
     */
    public void handleParentMessages(Update update) {

    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью VOLUNTEER
     * @param update обновление
     */
    public void handleVolunteerMessages(Update update) {

    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью ADMIN
     * @param update обновление
     */
    public void handleAdminMessages(Update update) {

    }

}
