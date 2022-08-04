package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.model.Update;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.constants.ButtonsText;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.MenuSelector;
import pro.sky.telegrambot.service.MenuStackService;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
public class UserMenuSelector implements MenuSelector {

    final MenuStackService menuStackService;
    /**
     * Логгер для класса
     */
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    @Value("${path.to.address.pic}")
    private String addressPath;
    private final java.io.File address = new File(addressPath);

    public UserMenuSelector(MenuStackService menuStackService) {
        this.menuStackService = menuStackService;
    }

    @Override
    public User.Role getRole() {
        return User.Role.USER;
    }

    @Override
    public void handleMessages(Function<String, Boolean> whatIsMenu,
                               BiConsumer<String, String> doSendMessage,
                               TriConsumer<File, String, String> doSendPhoto,
                               BiConsumer<Float, Float> doSendLocation,
                               User currentUser, Update update, ButtonsText buttonsText) {

//        Function<String, Boolean> whatIsMenu,00000000000000000000000000000000000
//        TriConsumer<File, String, String> doSendPhoto,
//        BiConsumer<Float, Float> doSendLocation,
        Function<String, Boolean> whatIsMenu = (someButtonNameKey) -> {
            String someButtonNameValue = buttonsText.getString(someButtonNameKey);
            String hashSomeButton = menuService.getHashFromButton(someButtonNameValue);
            return update.callbackQuery().data().equals(hashSomeButton);
        };
        BiConsumer<String, String> doSendMessage = (textKey, menuKey) -> {
            logger.info("====Processing doSendMessage with callback: {}", update.callbackQuery().data());
            String textPackKey = menuStackService.getLastTextPackKeyByUser(currentUser);
            MenuStack.MessageType expectedTypeCurrentMessage = menuStackService.getCurrentExpectedMessageTypeByUser(currentUser);
            menuStackService.createMenuStack(currentUser, textPackKey, textKey, menuKey, expectedTypeCurrentMessage);
            String textValue = buttonsText.getString(textKey);
            List<String> menuValue = buttonsText.getMenu(menuKey);
            telegramBot.execute(menuService.editMenuLoader(update, textValue, menuValue));
        };


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
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAIN_MENU");
        } else {
            doSendMessage.accept("ERROR_COMMAND_TEXT", "CALL_VOLUNTEER_MENU");
        }
    }
}
