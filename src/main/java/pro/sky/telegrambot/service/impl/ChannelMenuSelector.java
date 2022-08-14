package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.MenuService;
import pro.sky.telegrambot.service.MenuStackService;

import java.io.File;
import java.util.List;

import static pro.sky.telegrambot.model.MenuStack.MessageType.COMMAND;
import static pro.sky.telegrambot.model.MenuStack.MessageType.DIALOG;

@Service
public class ChannelMenuSelector extends AbstractMenuSelector{

    final MenuStackService menuStackService;
    final MenuService menuService;

    private final java.io.File address = new File("src/main/resources/MapPhoto/address.png");

    public ChannelMenuSelector(MenuStackService menuStackService,
                               MenuService menuService) {
        this.menuStackService = menuStackService;
        this.menuService = menuService;
    }

    @Override
    public User.Role getRole() {
        return User.Role.CHANNEL;
    }

    @Override
    public void handleMessages() {
            String command = update.callbackQuery().data();
            if (command.startsWith(buttonsText.getString("BEGIN_PREFIX"))) {
                String chatId = command.substring(buttonsText.getString("BEGIN_PREFIX").length());
                beginOrFinishDialog(update,
                        chatId,
                        DIALOG,
                        buttonsText.getString("CHAT_BEGIN_DIALOG_TEXT"),
                        buttonsText.getMenu("TO_SUPPORT_DENY_MENU"),
                        List.of(buttonsText.getString("FINISH_PREFIX") + chatId));
            }
            else if (command.startsWith(buttonsText.getString("FINISH_PREFIX"))) {
                String chatId = command.substring(buttonsText.getString("FINISH_PREFIX").length());
                beginOrFinishDialog(update,
                        chatId,
                        COMMAND,
                        buttonsText.getString("CHAT_FINISH_DIALOG_TEXT"),
                        null,
                        null);
            }
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
    private void beginOrFinishDialog(Update update, String chatId, MenuStack.MessageType messageType, String text, List<String> menu, List<String> callBack) {
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

}
