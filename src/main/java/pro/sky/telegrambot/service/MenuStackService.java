package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.User;

public interface MenuStackService {

    String getPreviousMenuStateByUser(User user);

    String getPreviousTextPackKeyByUser(User user);

    String getTextPackKeyByUser(User user);

    String getPreviousTextKeyByUser(User user);

    void setMenuState(User user, String menuState);

    void setTextPackKey(User user, String textPackKey);

    void setTextKey(User user, String textKey);

    MenuStack createMenuStack(User user);

    MenuStack createMenuStack(User user, String textPackKey, MenuStack.MessageType expected);

    void saveMenuStackParam(User user, String text, String menuStateKey);

    void dropMenuStack(User user);

    MenuStack.MessageType getCurrentExpectedMessageTypeByUser(User user);

    void setCurrentExpectedMessageTypeByUser(User user, MenuStack.MessageType expect);

}
