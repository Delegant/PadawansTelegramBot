package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.MenuService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;

import static pro.sky.telegrambot.constants.ButtonsText.*;
import static pro.sky.telegrambot.constants.ResponsesText.*;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");
    private final MenuService menuService;
    java.io.File address = new File("src/main/resources/MapPhoto/address.png");
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, MenuService menuService) {
        this.telegramBot = telegramBot;
        this.menuService = menuService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    private boolean hasCallbackQuery(Update update) {
        return update.callbackQuery() != null;
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            Message message = update.message();

            if (!hasCallbackQuery(update)) {
                if (message.text().equals("/start")) {
                    telegramBot.execute(menuService.menuLoader(message, START_TEXT, MAIN_MENU));
                }
            } else {
                telegramBot.execute((handleUserMessages((someButtonName) -> update.callbackQuery().data().equals(menuService.hashFromButton(someButtonName)),
                        (text, menu) -> {
                            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
                            return menuService.editMenu(update, text, menu);
                        }
                )));
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public EditMessageText handleUserMessages(Function<String, Boolean> whatIsMenu, BiFunction<String, List<String>, EditMessageText> getSendMessage) {
        EditMessageText result;
        if (whatIsMenu.apply(INFO_BUTTON)) {
            result = getSendMessage.apply(INFO_TEXT, INFO_MENU);
        } else if (whatIsMenu.apply(ABOUT_US_BUTTON)) {
            result = getSendMessage.apply(ABOUT_US, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(SAFETY_REGULATIONS_BUTTON)) {
            result = getSendMessage.apply(SAFETY_REGULATIONS, BACK_TO_MAIN_MENU);
//        } else if (whatIsMenu.apply(CONTACTS_BUTTON)) {
//            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
//            telegramBot.execute(new SendLocation(update.callbackQuery().message().chat().id(), 51.165973F, 71.403983F));
//            telegramBot.execute(new SendPhoto(update.callbackQuery().message().chat().id(), address));
//            telegramBot.execute(menuService.menuLoader(update, SHELTER_CONTACTS, BACK_TO_MAIN_MENU));
        } else if (whatIsMenu.apply(SHARE_CONTACT_BUTTON)) {
            result = getSendMessage.apply(SHARE_CONTACT, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(HOW_TO_GET_DOG_BUTTON)) {
            result = getSendMessage.apply(CONSULT_MENU_MESSAGE, HOW_TO_GET_DOG_MENU);
        } else if (whatIsMenu.apply(MEETING_WITH_DOG_BUTTON)) {
            result = getSendMessage.apply(MEETING_WITH_DOG, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(LIST_OF_DOCUMENTS_BUTTON)) {
            result = getSendMessage.apply(LIST_OF_DOCUMENTS, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(HOW_TO_CARRY_ANIMAL_BUTTON)) {
            result = getSendMessage.apply(HOW_TO_CARRY_ANIMAL, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(MAKING_HOUSE_BUTTON)) {
            result = getSendMessage.apply(DEFAULT_MENU_TEXT, MAKING_HOUSE_MENU);
        } else if (whatIsMenu.apply(FOR_PUPPY_BUTTON)) {
            result = getSendMessage.apply(MAKING_HOUSE_FOR_PUPPY, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(FOR_DOG_BUTTON)) {
            result = getSendMessage.apply(MAKING_HOUSE_FOR_DOG, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(FOR_DOG_WITH_DISABILITIES_BUTTON)) {
            result = getSendMessage.apply(MAKING_HOUSE_FOR_DOG_WITH_DISABILITIES, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(DOG_HANDLER_ADVICES_BUTTON)) {
            result = getSendMessage.apply(DOG_HANDLER_ADVICES, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(DOG_HANDLERS_BUTTON)) {
            result = getSendMessage.apply(DOG_HANDLERS, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(DENY_LIST_BUTTON)) {
            result = getSendMessage.apply(DENY_LIST, BACK_TO_MAIN_MENU);
        } else if (whatIsMenu.apply(BACK_TO_MAIN_MENU_BUTTON)) {
            result = getSendMessage.apply(DEFAULT_MENU_TEXT, MAIN_MENU);
        } else {
            result = getSendMessage.apply("Некоректная команда", MAIN_MENU);
        }
        return result;
    }

    public void handleParentMessages(Message message) {

    }

    public void handleVolunteerMessages(Message message) {

    }

    public void handleAdminMessages(Message message) {

    }

}
