package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.MenuStackRepository;
import pro.sky.telegrambot.service.MenuStackService;

@Service
public class MenuStackServiceImpl implements MenuStackService {

    private final MenuStackRepository menuStackRepository;

    public MenuStackServiceImpl(MenuStackRepository menuStackRepository) {
        this.menuStackRepository = menuStackRepository;
    }

    @Override
    public MenuStack createMenuStack(User user) {
        MenuStack menuStack = new MenuStack(user);
        return menuStackRepository.save(menuStack);
    }

    @Override
    public MenuStack createMenuStack(User user, String textPackKey) {
        MenuStack menuStack = new MenuStack(user);
        menuStack.setTextPackKey(textPackKey);
        return menuStackRepository.save(menuStack);
    }

    @Override
    public void saveMenuStackParam(User user, String textKey, String menuStateKey) {
        MenuStack menuStack = getMenuStackByUser(user);
        menuStack.setMenuState(menuStateKey);
        menuStack.setTextKey(textKey);
        menuStackRepository.save(menuStack);
    }

    @Override
    public void dropLastMenuStack(User user) {
        MenuStack menuStack = getMenuStackByUser(user);
        menuStackRepository.delete(menuStack);
    }

    @Override
    public void setTextPackKey(User user, String textPackKey) {
        MenuStack menuStack = getMenuStackByUser(user);
        menuStack.setTextPackKey(textPackKey);
        menuStackRepository.save(menuStack);
    }

    @Override
    public void setMenuState(User user, String menuState) {
        MenuStack menuStack = getMenuStackByUser(user);
        menuStack.setMenuState(menuState);
        menuStackRepository.save(menuStack);
    }

    @Override
    public void setTextKey(User user, String textKey) {
        MenuStack menuStack = getMenuStackByUser(user);
        menuStack.setTextKey(textKey);
        menuStackRepository.save(menuStack);
    }

    @Override
    public String getLastMenuStateByUser(User user) {
        return getLastMenuStackByUser(user).getMenuState();
    }

    @Override
    public String getLastTextPackKeyByUser(User user) {
        return getLastMenuStackByUser(user).getTextPackKey();
    }

    @Override
    public String getTextPackKeyByUser(User user) {
        return getMenuStackByUser(user).getTextPackKey();
    }

    @Override
    public String getLastTextKeyByUser(User user) {
        return getLastMenuStackByUser(user).getTextKey();
    }

    private MenuStack getMenuStackByUser(User user) {
        return menuStackRepository.findTopByUserOrderByIdDesc(user).orElseGet(()->createMenuStack(user));
    }

    private MenuStack getLastMenuStackByUser(User user) {
        return menuStackRepository.findLastMenuStateByUser(user).orElseGet(()->createMenuStack(user));
    }

}
