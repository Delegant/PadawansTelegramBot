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
import pro.sky.telegrambot.service.MenuService;

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

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");

    java.io.File address = new File("src/main/resources/MapPhoto/address.png");

    private TelegramBot telegramBot;

    private final MenuService menuService;

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

    public void handleParentMessages(Message message) {

    }

    public void handleVolunteerMessages(Message message) {

    }

    public void handleAdminMessages(Message message) {

    }

}
