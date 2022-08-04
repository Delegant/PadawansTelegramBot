package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.apache.logging.log4j.util.TriConsumer;
import pro.sky.telegrambot.constants.ButtonsText;
import pro.sky.telegrambot.model.User;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface MenuSelector {
    User.Role getRole();
    void handleMessages(Function<String, Boolean> whatIsMenu,
                        BiConsumer<String, String> doSendMessage,
                        TriConsumer<File, String, String> doSendPhoto,
                        BiConsumer<Float, Float> doSendLocation,
                        User currentUser, Update update, ButtonsText buttonsText);
}
