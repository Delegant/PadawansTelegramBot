package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.constants.ButtonsText;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.AdministrativeService;
import pro.sky.telegrambot.service.MenuSelector;
import pro.sky.telegrambot.service.MenuStackService;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AbstractMenuSelector implements MenuSelector {

    protected MenuStackService menuStackService;
    protected UserServiceImpl userService;
    protected AdministrativeService administrativeService;
    protected TelegramBot telegramBot;


    protected Function<String, Boolean> whatIsMenu;
    protected BiConsumer<String, String> doSendMessage;
    protected TriConsumer<File, String, String> doSendPhoto;
    protected BiConsumer<Float, Float> doSendLocation;
    protected BiConsumer<String, String> doSendHelpRequest;
    protected BiConsumer<String, String> doSendParentReportList;
    protected BiConsumer<String, String> doSendCustomMessage;
    protected BiConsumer<String, String> doSendReportList;
    protected BiConsumer<String, String> doSendUsersList;
    protected BiConsumer<String, String> doSendTrialPeriodsList;
    protected User currentUser;
    protected Update update;
    protected ButtonsText buttonsText;

    @Override
    public User.Role getRole() {
        return null;
    }

    @Override
    public void handleMessages() {
    }

    @Override
    public MenuSelector setWhatIsMenu(Function<String, Boolean> whatIsMenu) {
        this.whatIsMenu = whatIsMenu;
        return this;
    }

    @Override
    public MenuSelector setDoSendMessage(BiConsumer<String, String> doSendMessage) {
        this.doSendMessage = doSendMessage;
        return this;
    }

    @Override
    public MenuSelector setDoSendPhoto(TriConsumer<File, String, String> doSendPhoto) {
        this.doSendPhoto = doSendPhoto;
        return this;
    }

    @Override
    public MenuSelector setDoSendLocation(BiConsumer<Float, Float> doSendLocation) {
        this.doSendLocation = doSendLocation;
        return this;
    }

    @Override
    public MenuSelector setDoParentReportList(BiConsumer<String, String> doSendParentReportList) {
        this.doSendParentReportList = doSendParentReportList;
        return this;
    }

    @Override
    public MenuSelector setDoSendCustomMessage(BiConsumer<String, String> doSendCustomMessage) {
        this.doSendCustomMessage = doSendCustomMessage;
        return this;
    }

    public MenuSelector setDoSendHelpRequest(BiConsumer<String, String> doSendHelpRequest) {
        this.doSendHelpRequest = doSendHelpRequest;
        return this;
    }

    @Override
    public MenuSelector setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        return this;
    }

    @Override
    public MenuSelector setUpdate(Update update) {
        this.update = update;
        return this;
    }

    @Override
    public MenuSelector setButtonsText(ButtonsText buttonsText) {
        this.buttonsText = buttonsText;
        return this;
    }

    @Override
    public MenuSelector setDoSendParentReportList(BiConsumer<String, String> doSendParentReportList) {
        this.doSendParentReportList = doSendParentReportList;
        return this;
    }

    @Override
    public MenuSelector setDoSendReportList(BiConsumer<String, String> doSendReportList) {
        this.doSendReportList = doSendReportList;
        return this;
    }

    @Override
    public MenuSelector setDoSendUsersList(BiConsumer<String, String> doSendUsersList) {
        this.doSendUsersList = doSendUsersList;
        return this;
    }

    @Override
    public MenuSelector setDoSendTrialPeriodsList(BiConsumer<String, String> doSendTrialPeriodsList) {
        this.doSendTrialPeriodsList = doSendTrialPeriodsList;
        return this;
    }

}
