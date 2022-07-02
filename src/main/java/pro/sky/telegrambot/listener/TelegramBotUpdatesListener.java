package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.MenuService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

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
                handleUserMessages(update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public void handleUserMessages(Update update) {
        String action = update.callbackQuery().data();
        if (action.equals(INFO_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, INFO_TEXT, INFO_MENU));
        } else if (action.equals(ABOUT_US_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, ABOUT_US, BACK_TO_MAIN_MENU));
        } else if (action.equals(SAFETY_REGULATIONS_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, SAFETY_REGULATIONS, BACK_TO_MAIN_MENU));
        } else if (action.equals(CONTACTS_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(new SendLocation(update.callbackQuery().message().chat().id(), 51.165973F, 71.403983F));
            telegramBot.execute(new SendPhoto(update.callbackQuery().message().chat().id(), address));
            telegramBot.execute(menuService.menuLoader(update, SHELTER_CONTACTS, BACK_TO_MAIN_MENU));
        } else if (action.equals(SHARE_CONTACT_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, SHARE_CONTACT, BACK_TO_MAIN_MENU));
        } else if (action.equals(menuService.getCallBackData(HOW_TO_GET_DOG_BUTTON))) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, DEFAULT_MENU_TEXT, HOW_TO_GET_DOG_MENU));
        } else if (action.equals(MEETING_WITH_DOG_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, MEETING_WITH_DOG, BACK_TO_MAIN_MENU));
        } else if (action.equals(LIST_OF_DOCUMENTS_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, LIST_OF_DOCUMENTS, BACK_TO_MAIN_MENU));
        } else if (action.equals(HOW_TO_CARRY_ANIMAL_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, HOW_TO_CARRY_ANIMAL, BACK_TO_MAIN_MENU));
        } else if (action.equals(MAKING_HOUSE_FOR_PUPPY_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, MAKING_HOUSE_FOR_PUPPY, BACK_TO_MAIN_MENU));
        } else if (action.equals(menuService.getCallBackData(MAKING_HOUSE_FOR_DOG_BUTTON))) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, MAKING_HOUSE_FOR_DOG, BACK_TO_MAIN_MENU));
        } else if (action.equals(MAKING_HOUSE_FOR_DOG_WITH_DISABILITIES_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, MAKING_HOUSE_FOR_DOG_WITH_DISABILITIES, BACK_TO_MAIN_MENU));
        } else if (action.equals(DOG_HANDLER_ADVICES_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, DOG_HANDLER_ADVICES, BACK_TO_MAIN_MENU));
        } else if (action.equals(DOG_HANDLERS_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, DOG_HANDLERS, BACK_TO_MAIN_MENU));
        } else if (action.equals(DENY_LIST_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, DENY_LIST, BACK_TO_MAIN_MENU));
        } else if (action.equals(BACK_TO_MAIN_MENU_BUTTON)) {
            logger.info("==== Processing update with callback: {}", update.callbackQuery().data());
            telegramBot.execute(menuService.menuLoader(update, DEFAULT_MENU_TEXT, MAIN_MENU));
        }
    }

    public void handleParentMessages(Message message) {

    }

    public void handleVolunteerMessages(Message message) {

    }

    public void handleAdminMessages(Message message) {

    }

}
