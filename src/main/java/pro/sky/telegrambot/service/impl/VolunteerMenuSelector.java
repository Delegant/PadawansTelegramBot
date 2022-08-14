package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.AdministrativeService;
import pro.sky.telegrambot.service.MenuStackService;

import static pro.sky.telegrambot.model.MenuStack.MessageType.*;

@Service
public class VolunteerMenuSelector extends AbstractMenuSelector {

    public VolunteerMenuSelector(MenuStackService menuStackService,
                                 TelegramBot telegramBot,
                                 UserServiceImpl userService,
                                 AdministrativeService administrativeService) {
        this.menuStackService = menuStackService;
        this.userService = userService;
        this.administrativeService = administrativeService;
        this.telegramBot = telegramBot;
    }

    @Override
    public User.Role getRole() {
        return User.Role.VOLUNTEER;
    }

    /**
     * Метод, обрабатывающий сообщения и нажатия кнопок от пользователя с ролью VOLUNTEER
     */
    public void handleMessages() {

        if (whatIsMenu.apply("START_BUTTON")) {
            doSendMessage.accept("VOLUNTEER_START_TEXT", "VOLUNTEER_MAIN_MENU");
        } else if (menuStackService.getCurrentExpectedMessageTypeByUser(currentUser).equals(USER_NAME)) {
            doSendUsersList.accept("CHOOSE_USER_TO_MAKE_PARENT", "BACK_TO_VOLUNTEERS_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, ADDING_PARENT);
        } else if  (whatIsMenu.apply("ADD_PARENT_BUTTON_VOLUNTEER")) {
            doSendMessage.accept("ADD_PARENT_TEXT", "BACK_TO_VOLUNTEERS_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, USER_NAME);
        }
        else if (whatIsMenu.apply("CHECK_REPORTS_BUTTON")) {
            doSendReportList.accept("CHECK_REPORTS", "REPORTS_MENU");
        }
        else if (whatIsMenu.apply("UNREAD_REPORTS")) {
            doSendReportList.accept("UNREAD_REPORTS_TEXT", "BACK_TO_VOLUNTEERS_MENU");
        } else if (whatIsMenu.apply("BACK_TO_REPORT_LIST")) {
            doSendReportList.accept("UNREAD_REPORTS_TEXT", "BACK_TO_VOLUNTEERS_MENU");
        }
        else if (whatIsMenu.apply("VOLUNTEER_MAIN_MENU_BUTTON")) {
            doSendMessage.accept("VOLUNTEER_START_TEXT", "VOLUNTEER_MAIN_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
        } else if (whatIsMenu.apply("TRIAL_PERIODS")) {
            doSendTrialPeriodsList.accept("VOLUNTEER_START_TEXT", "VOLUNTEER_MAIN_MENU");
        } else if (menuStackService.getCurrentExpectedMessageTypeByUser(currentUser).equals(ADDING_PARENT)) {
            User newParent = userService.getUser(Long.valueOf(update.callbackQuery().data()));
            administrativeService.setParent(currentUser.getChatId(), newParent.getChatId());
            telegramBot.execute(new SendMessage(newParent.getChatId(), "Поздравляем, вы взяли питомца. Ваш испытательный период начался!"));
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
            doSendMessage.accept("AFTER_ADDING_PARENT", "VOLUNTEER_MAIN_MENU");
        }
        else {
            doSendMessage.accept("ERROR_TEXT", "VOLUNTEER_MAIN_MENU");
        }
    }

}
