package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.User;

import java.util.List;

public interface MenuStackService {

    String getLastMenuStateByUser(User user);

    String getLastTextPackKeyByUser(User user);

    String getLastTextKeyByUser(User user);

    void setMenuState(User user, String menuState);

    void setTextPackKey(User user, String textPackKey);

    void setTextKey(User user, String textKey);

    void setExpectedMessageTypeByRole(User.Role role, MenuStack.MessageType messageType);

    MenuStack createMenuStack(User user);

    MenuStack createMenuStack(User user, String textPackKey, MenuStack.MessageType expected);

    MenuStack createMenuStack(User user, String textPackKey, String textKey, String menuState, MenuStack.MessageType expected);

    void saveMenuStackParam(User user, String text, String menuStateKey);

    void dropMenuStack(User user);

    MenuStack.MessageType getCurrentExpectedMessageTypeByUser(User user);

    void setCurrentExpectedMessageTypeByUser(User user, MenuStack.MessageType expect);

}
