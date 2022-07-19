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

    /**
     * Метод, удаляет строку положения пользователя,
     * соответсвующую в текущему меню
     *
     * @param user пользователя для поиска из репозитория
     */
    @Override
    public void dropMenuStack(User user) {
        MenuStack menuStack = getMenuStackByUser(user);
        menuStackRepository.delete(menuStack);
    }

    @Override
    public MenuStack.ExpectedMessageType getCurrentExpectedMessageTypeByUser(User user) {
        return getMenuStackByUser(user).getExpect();
    }

    @Override
    public void setCurrentExpectedMessageTypeByUser(User user, MenuStack.ExpectedMessageType expect) {
        MenuStack menuStack = getMenuStackByUser(user);
        menuStack.setExpect(expect);
        menuStackRepository.save(menuStack);
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

    /**
     * Метод, возвращает строку с именем меню,
     * в которое пользователь попал в прошлый update
     *
     * @param user пользователя для поиска из репозитория
     * @return возвращает ключ для поиска в хранилище меню
     */
    @Override
    public String getPreviousMenuStateByUser(User user) {
        return getPreviousMenuStackByUser(user).getMenuState();
    }

    /**
     * Метод, возвращает строку с кэшем кнопки
     * выбора типа животного, в прошлый update.
     *
     * @param user пользователя для поиска из репозитория
     * @return возвращает результат функции Objects.hash()
     * на тексте кнопки
     */
    @Override
    public String getPreviousTextPackKeyByUser(User user) {
        return getPreviousMenuStackByUser(user).getTextPackKey();
    }

    /**
     * Метод, возвращает строку с кэшем кнопки
     * выбора типа животного, в текущий update.
     *
     * @param user пользователя для поиска из репозитория
     * @return возвращает результат функции Objects.hash()
     * на тексте кнопки
     */
    @Override
    public String getTextPackKeyByUser(User user) {
        return getMenuStackByUser(user).getTextPackKey();
    }

    /**
     * Метод, возвращает строку с текстом меню,
     * который пользователь получил в прошлый update
     *
     * @param user пользователя для поиска из репозитория
     * @return возвращает ключ для поиска в хранилище текстов
     */
    @Override
    public String getPreviousTextKeyByUser(User user) {
        return getPreviousMenuStackByUser(user).getTextKey();
    }

    private MenuStack getMenuStackByUser(User user) {
        return menuStackRepository.findTopByUserOrderByIdDesc(user).orElseGet(() -> createMenuStack(user));
    }

    private MenuStack getPreviousMenuStackByUser(User user) {
        return menuStackRepository.findLastMenuStateByUser(user).orElseGet(() -> createMenuStack(user));
    }

}
