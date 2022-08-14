package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.apache.logging.log4j.util.TriConsumer;
import pro.sky.telegrambot.constants.ButtonsText;
import pro.sky.telegrambot.model.User;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public interface MenuSelector {
    User.Role getRole();
    void handleMessages();

    MenuSelector setWhatIsMenu(Function<String, Boolean> whatIsMenu);
    MenuSelector setDoSendMessage(BiConsumer<String, String> doSendMessage);
    MenuSelector setDoSendPhoto(TriConsumer<File, String, String> doSendPhoto);
    MenuSelector setDoSendLocation(BiConsumer<Float, Float> doSendLocation);
    MenuSelector setDoParentReportList(BiConsumer<String, String> doSendParentReportList);
    MenuSelector setDoSendCustomMessage(BiConsumer<String, String> doSendCustomMessage);
    MenuSelector setDoSendReportList(BiConsumer<String, String> doSendReportList);
    MenuSelector setDoSendParentReportList(BiConsumer<String, String> doSendParentReportList);
    MenuSelector setDoSendUsersList(BiConsumer<String, String> doSendUsersList);
    MenuSelector setCurrentUser(User currentUser);
    MenuSelector setUpdate(Update update);
    MenuSelector setButtonsText(ButtonsText buttonsText);
    MenuSelector setDoSendTrialPeriodsList(BiConsumer<String, String> doSendTrialPeriodsList);
    MenuSelector setDoSendHelpRequest(BiConsumer<String, String> doSendHelpRequest);
}
