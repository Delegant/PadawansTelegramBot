package pro.sky.telegrambot.component;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.constants.ButtonsText;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.*;
import pro.sky.telegrambot.service.impl.UserServiceImpl;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static pro.sky.telegrambot.model.MenuStack.MessageType.*;
import static pro.sky.telegrambot.model.MenuStack.MessageType.REPORT_ACTION;

@Component
public class MenuSelectorFactory {

    /**
     * Логгер для класса
     */
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    /**
     * Коллекция селекторов меню
     */
    private final Map<User.Role, MenuSelector> viewerMap = new EnumMap<>(User.Role.class);
    /**
     * Инжектированный сервис-класс, отвечающий за обработку и создание клавиатур
     *
     * @see MenuService
     */
    private final MenuService menuService;
    /**
     * Инжектированный сервис-класс, отвечающий за отслеживанием положения
     * пользователей в БД
     *
     * @see MenuService
     */
    private final MenuStackService menuStackService;
    private final TelegramBot telegramBot;
    @Value("${pro.sky.channel.id}")
    private Long channelId;

    @Autowired
    private MenuSelectorFactory(List<MenuSelector> menuSelectors,
                                TelegramBot telegramBot,
                                MenuService menuService,
                                MenuStackService menuStackService) {
        this.telegramBot = telegramBot;
        this.menuService = menuService;
        this.menuStackService = menuStackService;

        for (MenuSelector menuSelector : menuSelectors) {
            viewerMap.put(menuSelector.getRole(), menuSelector);
        }
    }

    public MenuSelector getMenuSelectorForCallBack(User.Role role, User currentUser, Update update, ButtonsText buttonsText) {
        MenuSelector menuSelector = getMenuSelector(role);
        menuSelector.setWhatIsMenu((someButtonNameKey) -> {
                    String someButtonNameValue = buttonsText.getString(someButtonNameKey);
                    String hashSomeButton = menuService.getHashFromButton(someButtonNameValue);
                    if (update.callbackQuery() == null) {
                        return false;
                    }
                    return update.callbackQuery().data().equals(hashSomeButton);
                })
                .setDoSendMessage((textKey, menuKey) -> {
                    logger.info("====Processing doSendMessage with callback: {}", update.callbackQuery().data());
                    String textPackKey = menuStackService.getLastTextPackKeyByUser(currentUser);
                    MenuStack.MessageType expectedTypeCurrentMessage = menuStackService.getCurrentExpectedMessageTypeByUser(currentUser);
                    menuStackService.createMenuStack(currentUser, textPackKey, textKey, menuKey, expectedTypeCurrentMessage);
                    String textValue = buttonsText.getString(textKey);
                    List<String> menuValue = buttonsText.getMenu(menuKey);
                    telegramBot.execute(menuService.editMenuLoader(update, textValue, menuValue));
                })
                .setDoSendPhoto((filePath, textKey, buttonsValue) -> {
                    logger.info("====Processing update with callback: {}", update.callbackQuery().data());
                    List<String> buttons = buttonsText.getMenu(buttonsValue);
                    String textValue = buttonsText.getString(textKey);
                    telegramBot.execute(menuService.sendLocationPhotoLoader(update, filePath, textValue, buttons));
                })
                .setDoSendLocation((latitude, longitude) -> {
                    logger.info("==== Sending location: {}", update.callbackQuery().data());
                    telegramBot.execute(menuService.sendLocationLoader(update, latitude, longitude));
                })
                .setDoSendHelpRequest((textKey, menuKey) -> {
                    logger.info("====Processing doSendMessage with callback: {}", update.callbackQuery().data());
                    telegramBot.execute(menuService.sendTextLoader(
                            channelId,
                            buttonsText.getString(textKey),
                            buttonsText.getMenu(menuKey),
                            List.of(buttonsText.getString("BEGIN_PREFIX") + currentUser.getChatId().toString())));
                })
                .setDoParentReportList((textToSend, menuKey) -> {
                    ///TODO Поправить тип ожидаемого сообщения и как следствие выводимое меню для парента (сейчас выводит как для волонтера)
                    logger.info("==== Sending list of Reports: {}", update.callbackQuery().data());
                    List<List<String>> buttons = menuService.generateListOfAllUserReports(currentUser.getChatId());
                    String text = buttonsText.getString(textToSend);
                    if (buttons.size() == 0) {
                        telegramBot.execute(menuService.menuLoader(update, "Вы не написали еще ни одного отчета!", buttonsText.getMenu("MAIN_MENU")));
                    } else {
                        if (Objects.equals(menuKey, "ALL")) {
                            telegramBot.execute(menuService.menuLoaderForObjects(update, text, menuService.generateListOfAllUserReports(currentUser.getChatId())));
                        } else {
                            telegramBot.execute(menuService.menuLoaderForObjects(update, text, menuService.generateListOfUpdateRequestedUserReports(currentUser.getChatId())));
                        }
                        menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, MY_REPORTS);
                    }
                })
                .setDoSendCustomMessage((textToSend, menuKey) -> {
                    logger.info("==== Sending custom message to user with ig = {}", currentUser.getChatId());
                    List<String> menuValue = buttonsText.getMenu(menuKey);
                    telegramBot.execute(menuService.editMenuLoader(update, textToSend, menuValue));
                })
                .setDoSendTrialPeriodsList((menuKey, textKey) -> {
                    logger.info("==== Sending list of Trial_Periods: {}", update.callbackQuery().data());
                    List<List<String>> buttons = menuService.generateListOfAllTrialPeriods();
                    String menuText = buttonsText.getString(menuKey);
                    if (buttons.size() == 0) {
                        telegramBot.execute(menuService.menuLoader(update, "Список испытательных периодов пуст!", buttonsText.getMenu("VOLUNTEER_MAIN_MENU")));
                        menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, COMMAND);
                    }
                    else {
                        telegramBot.execute(menuService.menuLoaderForObjects(update, menuText, buttons));
                        menuStackService.setCurrentExpectedMessageTypeByUser(currentUser, TRIAL_PERIOD_LIST);
                        }
                })
                .setCurrentUser(currentUser)
                .setUpdate(update)
                .setButtonsText(buttonsText);
        return menuSelector;
    }

    public MenuSelector getMenuSelectorForTextCommand(User.Role role, Message message, ButtonsText buttonsText) {
        MenuSelector menuSelector = getMenuSelector(role);
        menuSelector.setWhatIsMenu((someButtonName) -> message.text().equals(buttonsText.getString(someButtonName)));
        menuSelector.setDoSendMessage((textKey, menuKey) -> {
            logger.info("====Processing update with message: {}", message.text());
            List<String> menuValue = buttonsText.getMenu(menuKey);
            String textValue = buttonsText.getString(textKey);
            telegramBot.execute(menuService.menuLoader(message, textValue, menuValue));
        });
        return menuSelector;
    }

    private MenuSelector getMenuSelector(User.Role role) {
        MenuSelector menuSelector = viewerMap.get(role);
        if (menuSelector == null) {
            throw new IllegalArgumentException();
        }
        return menuSelector;
    }

}
