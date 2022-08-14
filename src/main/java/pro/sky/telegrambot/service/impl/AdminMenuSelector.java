package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.MenuStackService;

@Service
public class AdminMenuSelector extends AbstractMenuSelector {

    final MenuStackService menuStackService;

    public AdminMenuSelector(MenuStackService menuStackService) {
        this.menuStackService = menuStackService;
    }

    @Override
    public User.Role getRole() {
        return User.Role.ADMIN;
    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью ADMIN
     */
    @Override
    public void handleMessages() {
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
