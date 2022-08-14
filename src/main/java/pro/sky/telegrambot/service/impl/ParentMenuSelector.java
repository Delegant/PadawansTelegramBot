package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.MenuStackService;
import pro.sky.telegrambot.service.TrialPeriodService;

import static pro.sky.telegrambot.model.MenuStack.MessageType.*;

@Service
public class ParentMenuSelector extends UserMenuSelector {

    private final TrialPeriodService trialPeriodService;

    public ParentMenuSelector(MenuStackService menuStackService, TrialPeriodService trialPeriodService) {
        super(menuStackService);
        this.trialPeriodService = trialPeriodService;
    }

    @Override
    public User.Role getRole() {
        return User.Role.PARENT;
    }

    @Override
    public void handleMessages() {

        if (whatIsMenu.apply("SEND_REPORT_BUTTON")) {
            doSendMessage.accept("SEND_REPORT_TEXT", "BACK_TO_MAIN_MENU");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, REPORT);
        } else if (whatIsMenu.apply("FINISH_SENDING_REPORT")) {
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAIN_MENU");
        } else if (whatIsMenu.apply("MY_REPORTS")) {
            doSendMessage.accept("WHAT_NEXT_TEXT", "MY_REPORTS_MENU");
        } else if (whatIsMenu.apply("ALL_REPORTS")) {
            doSendParentReportList.accept("ALL_REPORTS", "ALL");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, MY_REPORTS);
        } else if (whatIsMenu.apply("UPDATE_REQUESTED")) {
            doSendParentReportList.accept("UPDATE_REQUESTED", "TO_BE_UPDATED");
            menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, MY_REPORTS);
        } else if (whatIsMenu.apply("MY_TRIAL_PERIOD")) {
            doSendCustomMessage.accept(trialPeriodService.getTrialPeriodInformation(currentUser.getChatId()), "MAIN_MENU");
        } else if (whatIsMenu.apply("BACK_TO_MAIN_MENU_BUTTON")) {
            doSendMessage.accept("DEFAULT_MENU_TEXT", "MAIN_MENU");
        } else {
            super.handleMessages();
        }
    }
}
