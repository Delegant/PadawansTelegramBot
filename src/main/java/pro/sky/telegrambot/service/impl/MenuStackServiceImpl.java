package pro.sky.telegrambot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.MenuStackRepository;
import pro.sky.telegrambot.service.MenuStackService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuStackServiceImpl implements MenuStackService {

    private final MenuStackRepository menuStackRepository;
    /**
     * Логгер для класса
     */
    private final Logger logger = LoggerFactory.getLogger(MenuStackServiceImpl.class);


    public MenuStackServiceImpl(MenuStackRepository menuStackRepository) {
        this.menuStackRepository = menuStackRepository;
    }

    /**
     * Метод, для создания нового положения в меню
     * только на основе конкретного пользователя
     *
     * @param user пользователя для поиска из репозитория
     */
    @Override
    public MenuStack createMenuStack(User user) {
        logger.info("====Processing create MenuStack with user: {}", user);
        MenuStack menuStack = new MenuStack(user);
        return menuStackRepository.save(menuStack);
    }

    /**
     * Метод, для создания нового положения в меню
     * с учетом уже выбранного текстового пакета и
     * с учетом ожидаемого типа сообщений
     *
     * @param user        пользователя для поиска из репозитория
     * @param textPackKey языковой пакет пользователя
     * @param expected ожидаемый тип сообщений
     */
    @Override
    public MenuStack createMenuStack(User user, String textPackKey, MenuStack.MessageType expected) {
        logger.info("====Processing create MenuStack with textPackKey: {} and user: {}", textPackKey, user);
        MenuStack menuStack = new MenuStack(user);
        menuStack.setTextPackKey(textPackKey);
        menuStack.setExpect(expected);
        return menuStackRepository.save(menuStack);
    }

    @Override
    public MenuStack createMenuStack(User user, String textPackKey, String textKey, String menuStateKey, MenuStack.MessageType expectedTypeCurrentMessage) {
        logger.info("====Processing create MenuStack with textPackKey {}, menuStateKey: {}, textKey: {} and expectedTypeCurrentMessage {}", textPackKey, menuStateKey, textKey, expectedTypeCurrentMessage);
        MenuStack menuStack = new MenuStack(user);
        menuStack.setTextPackKey(textPackKey);
        menuStack.setMenuState(menuStateKey);
        menuStack.setTextKey(textKey);
        menuStack.setExpect(expectedTypeCurrentMessage);
        return menuStackRepository.save(menuStack);
    }

    @Override
    public void saveMenuStackParam(User user, String textKey, String menuStateKey) {
        logger.info("====Processing create MenuStack with menuStateKey: {} and textKey: {}", menuStateKey, textKey);
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
    public MenuStack.MessageType getCurrentExpectedMessageTypeByUser(User user) {
        return getMenuStackByUser(user).getExpect();
    }

    /**
     * Метод, задает ожидаемый тип следующего update
     *
     * @param user пользователя для поиска из репозитория
     * @param expect ожидаемый тип
     * @see MenuStack.MessageType
     */
    @Override
    public void setCurrentExpectedMessageTypeByUser(User user, MenuStack.MessageType expect) {
        logger.info("====Processing create MenuStack with MessageType: {}", expect);
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
     * Метод, возвращает ожидаемый тип следующего update
     *
     * @param user пользователя для поиска из репозитория
     * @return ожидаемый тип
     * @see MenuStack.MessageType
     */
    @Override
    public MenuStack.MessageType getCurrentExpectedMessageTypeByUser(User user) {
        return getMenuStackByUser(user).getExpect();
    }

    /**
     * Метод, возвращает строку с именем меню,
     * в которое пользователь попал в прошлый update
     *
     * @param user пользователя для поиска из репозитория
     * @return возвращает ключ для поиска в хранилище меню
     */
    @Override
    public String getLastMenuStateByUser(User user) {
        return getMenuStackByUser(user).getMenuState();
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
    public String getLastTextPackKeyByUser(User user) {
        return getMenuStackByUser(user).getTextPackKey();
    }

    /**
     * Метод, возвращает строку с текстом ответа,
     * который пользователь получил в прошлый update
     *
     * @param user пользователя для поиска из репозитория
     * @return возвращает ключ для поиска в хранилище текстов
     */
    @Override
    public String getLastTextKeyByUser(User user) {
        return getMenuStackByUser(user).getTextKey();
    public String getLastTextKeyByUser(User user) {
        return getMenuStackByUser(user).getTextKey();
    }

    /**
     * Метод, сохраняет новый ожидаемый тип следующего сообщения
     * всем записям репозитория соответствующим переданной роли.
     * @param role роль для поиска из репозитория
     * @param messageType ожидаемый тип следующего сообщения
     */
    @Override
    public void setExpectedMessageTypeByRole(User.Role role, MenuStack.MessageType messageType) {
        List<MenuStack> menuStacks = getAllLastMenuStackByUserRole(role).stream().peek(menuStack -> menuStack.setExpect(messageType)).collect(Collectors.toList());
        menuStackRepository.saveAll(menuStacks);
    }

    private MenuStack getMenuStackByUser(User user) {
        return menuStackRepository.findTopByUserOrderByIdDesc(user).orElseGet(() -> createMenuStack(user));
    }

    private List<MenuStack> getAllLastMenuStackByUserRole(User.Role role) {
        return menuStackRepository.findAllLastMenuStackByUserRole(role);
    }

}
