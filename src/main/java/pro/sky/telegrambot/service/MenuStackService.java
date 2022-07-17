package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.User;

import java.util.Optional;

public interface MenuStackService {

    String getLastMenuStateByUser(User user);

    String  getLastTextPackKeyByUser(User user);

    String  getTextPackKeyByUser(User user);

    String  getLastTextKeyByUser(User user);

    void setMenuState(User user, String menuState);

    void setTextPackKey(User user, String textPackKey);

    void setTextKey(User user, String textKey);

    MenuStack createMenuStack(User user);

    MenuStack createMenuStack(User user, String textPackKey);

    void saveMenuStackParam(User user, String text, String menuStateKey);

    void dropLastMenuStack(User user);

}
