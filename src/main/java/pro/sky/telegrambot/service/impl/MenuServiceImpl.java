package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.Dao.Impl.ReportDao;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.model.PictureName;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.service.MenuService;
import pro.sky.telegrambot.service.ReportService;
import pro.sky.telegrambot.service.UserService;

import static pro.sky.telegrambot.constants.ButtonsText.HIDDEN_BUTTON;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс, создающий сообщения с inline-клавиатурой
 * @author Kulachenkov, algmironov
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final Logger logger = LoggerFactory.getLogger(MenuServiceImpl.class);
    @Autowired
    private ReportDao reportDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private PicturesRepository picturesRepository;

    public InlineKeyboardMarkup getMainKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.addRow(new InlineKeyboardButton("Вернуться в главное меню").callbackData(getHashFromButton("/start")));
        return keyboardMarkup;
    }

    @Deprecated
    private ReplyKeyboardMarkup keyboardMaker(List<String> list) {
        KeyboardButton[] buttons = new KeyboardButton[list.size()];
        for (int i = 0; i < list.size(); i++) {
            KeyboardButton name = new KeyboardButton(list.get(i));
            buttons[i] = name;
        }

        return new ReplyKeyboardMarkup(buttons);
    }




    /**
     * Метод, принимающий список кнопок и формирующий клавиатуру для вставки в сообщение.
     * @param list - входящий список кнопок (текстов для кнопок)
     * @return - возвращает inline-клавиатуру
     * @see MenuServiceImpl#menuLoader(Message, String, List)
     * @see MenuServiceImpl#menuLoader(Update, String, List)
     */
    private InlineKeyboardMarkup keyboardFactory(List<String> list) {
        if (list == null) {
            throw new NullPointerException("Inline menu list have null");
        }
        list = list.stream().filter(buttonText -> !buttonText.equals(HIDDEN_BUTTON)).collect(Collectors.toList());
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        if (list.size() <= 10) {
            for (int i = 0; i < list.size(); i++) {
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton(list.get(i))
                                .callbackData(getHashFromButton(list.get(i))));
            }
        }
        if (list.size() > 10 && list.size() % 2 == 0) {
            for (int i = 0; i < list.size(); i = i + 2) {
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton(list.get(i))
                                .callbackData(getHashFromButton(list.get(i))),
                        new InlineKeyboardButton(list.get(i + 1))
                                .callbackData(getHashFromButton(list.get(i + 1))));
            }
        }
        if (list.size() > 10 && list.size() % 2 != 0) {
            for (int i = 0; i < list.size() - 1; i = i + 2) {
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton(list.get(i))
                                .callbackData(getHashFromButton(list.get(i))),
                        new InlineKeyboardButton(list.get(i + 1))
                                .callbackData(getHashFromButton(list.get(i + 1))));
            }
            inlineKeyboardMarkup.addRow(
                    new InlineKeyboardButton(list.get(list.size() - 1))
                            .callbackData(getHashFromButton(list.get(list.size() - 1))));
        }
        return inlineKeyboardMarkup;
    }

    /**
     * Метод, принимающий список кнопок и формирующий клавиатуру для вставки в сообщение.
     * @param list - входящий список кнопок (текстов для кнопок)
     * @return - возвращает inline-клавиатуру
     * @see MenuServiceImpl#menuLoaderForObjects(Message, String, List) 
     * @see MenuServiceImpl#menuLoaderForObjects(Update, String, List) 
     */
    private InlineKeyboardMarkup keyboardFactoryForObjects(List<List<String>> list) {
        if (list == null) {
            throw new NullPointerException("Inline menu list is null");
        }
        list.removeIf(elem -> elem.get(0).equals(HIDDEN_BUTTON));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        if (list.size() <= 10) {
            for (List<String> strings : list) {
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton(strings.get(0))
                                .callbackData(strings.get(1)));
            }
        }
        if (list.size() > 10 && list.size() % 2 == 0) {
            for (int i = 0; i < list.size(); i = i + 2) {
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton(list.get(i).get(0))
                                .callbackData(list.get(i).get(1)),
                        new InlineKeyboardButton(list.get(i + 1).get(0))
                                .callbackData(list.get(i + 1).get(1)));
            }
        }
        if (list.size() > 10 && list.size() % 2 != 0) {
            for (int i = 0; i < list.size() - 1; i = i + 2) {
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton(list.get(i).get(0))
                                .callbackData(list.get(i).get(1)),
                        new InlineKeyboardButton(list.get(i + 1).get(0))
                                .callbackData(list.get(i + 1).get(1)));
            }
            inlineKeyboardMarkup.addRow(
                    new InlineKeyboardButton(list.get(list.size() - 1).get(0))
                            .callbackData(list.get(list.size() - 1).get(1)));
        }
        return inlineKeyboardMarkup;
    }



    /**
     * Метод, формирующий новое сообщение из входящих параметров:
     * @param message - Из поля message берется id чата, куда будет отправлено сообщение
     * @param text - текст отправляемого сообщения
     * @param listButtons - список кнопок (текстов кнопок) для клавиатуры
     *                    @see MenuServiceImpl#keyboardFactory(List)
     * @return - возвращает новое сформированное сообщение
     */
    @Override
    public SendMessage menuLoader (Message message, String text, List<String> listButtons) {
        if (message == null || text == null || listButtons == null) {
            throw new NullPointerException("!!!! One or more parameter is null");
        }
        try {
            Keyboard keyboard = keyboardFactory(listButtons);
            return new SendMessage(message.chat().id(), text)
                    .replyMarkup(keyboard);
        } catch (RuntimeException e) {
            throw new RuntimeException("The list of buttons is invalid");
        }
    }

    @Deprecated
    public SendMessage replyKeyboardLoader(List<String> list, String text, Message message) {
        if (message == null || text == null || list == null) {
            throw new NullPointerException("!!!! One or more parameter is null");
        }
        SendMessage msg = new SendMessage(message.chat().id(), text);
        try {
            Keyboard keyboard = keyboardMaker(list);

            msg.replyMarkup(keyboard);
//            return new SendMessage(message.chat().id(), text)
//                    .replyMarkup(keyboard);
        } catch (RuntimeException e) {
            throw new RuntimeException("The list of buttons is invalid");
        }
        return msg;
    }

    @Deprecated
    @Override
    public SendMessage replyKeyboardLoader(List<String> list, String text, Update update) {
        if (update == null || text == null || list == null) {
            throw new NullPointerException("!!!! One or more parameter is null");
        }
        SendMessage msg = new SendMessage(update.message().chat().id(), text);
        try {
            Keyboard keyboard = keyboardMaker(list);
            msg.replyMarkup(keyboard);
//            return new SendMessage(update.callbackQuery().message().chat().id(), text)
//                    .replyMarkup(keyboard);
        } catch (RuntimeException e) {
            throw new RuntimeException("The list of buttons is invalid");
        }
        return msg;
    }

    /**
     * Перегруженный метод, формирующий новое сообщение из входящих параметров:
     * @param update - Из поля update берется id чата, куда будет отправлено сообщение
     *               (в данном случае update применяется вместо message, поскольку при нажатии inline кнопки
     *               поле message в update равно null)
     * @param text - текст отправляемого сообщения
     * @param listButtons - список кнопок (текстов кнопок) для клавиатуры
     *                    @see MenuServiceImpl#keyboardFactory(List)
     * @return - возвращает новое сформированное сообщение
     */
    @Override
    public SendMessage menuLoader(Update update, String text, List<String> listButtons) {
        if (update == null || text == null || listButtons == null) {
            throw new NullPointerException("!!!! One or more parameter is null");
        }
        try{
        Keyboard keyboard = keyboardFactory(listButtons);
        return new SendMessage(update.callbackQuery().message().chat().id(), text)
                .replyMarkup(keyboard);
        } catch (RuntimeException e) {
            throw new RuntimeException("The list of buttons is invalid");
        }
    }

    /**
     *  Метод отправляет список пользователей или очетов с колбеком в виде id 
     *  Это нужно для правильной обработки колбеков в дальнейшем
     * @param message сообщение из Телеграм
     * @param text текст сообщения
     * @param buttonsWithCallbacks список списков с названиями кнопок и колбеками
     * @return новое сообщение
     */
    public SendMessage menuLoaderForObjects(Message message, String text, List<List<String>> buttonsWithCallbacks) {
        if (message == null || text == null || buttonsWithCallbacks == null) {
            throw new NullPointerException("!!!! One or more parameter is null");
        }
        List<String> backButton = List.of("Назад", getHashFromButton("Назад"));
        buttonsWithCallbacks.add(backButton);
        try {
            Keyboard keyboard = keyboardFactoryForObjects(buttonsWithCallbacks);
            return new SendMessage(message.chat().id(), text)
                    .replyMarkup(keyboard);
        } catch (RuntimeException e) {
            throw new RuntimeException("The list of buttons is invalid");
        }

    }

    /**
     *  Метод отправляет список пользователей или очетов с колбеком в виде id 
     *  Это нужно для правильной обработки колбеков в дальнейшем
     * @param update апдейт из Телеграм
     * @param text текст сообщения
     * @param buttonsWithCallbacks список списков с названиями кнопок и колбеками
     * @return новое сообщение
     */
    public SendMessage menuLoaderForObjects(Update update, String text, List<List<String>> buttonsWithCallbacks) {
        if (update == null || text == null || buttonsWithCallbacks == null) {
            throw new NullPointerException("!!!! One or more parameter is null");
        }
        List<String> backButton = List.of("Назад", getHashFromButton("Назад"));
        buttonsWithCallbacks.add(backButton);
        try{
            Keyboard keyboard = keyboardFactoryForObjects(buttonsWithCallbacks);
            return new SendMessage(update.callbackQuery().message().chat().id(), text)
                    .replyMarkup(keyboard);
        } catch (RuntimeException e) {
            throw new RuntimeException("The list of buttons is invalid");
        }
    }

    /**
     * Перегруженный метод, формирующий новое сообщение из входящих параметров:
     * @param chatId - id чата, куда будет отправлено сообщение
     * @param text - текст отправляемого сообщения
     * @return - возвращает новое сформированное сообщение
     */
    public SendMessage sendTextLoader(Long chatId, String text) {
        try{
            return new SendMessage(chatId, text);
        } catch (RuntimeException e) {
            throw new RuntimeException("The list of buttons is invalid");
        }
    }

    /**
     * Перегруженный метод, формирующий новое сообщение из входящих параметров:
     * @param chatId - id чата, куда будет отправлено сообщение
     * @param text - текст отправляемого сообщения
     * @param listButtons - лист кнопок для меню
     * @return - возвращает новое сформированное сообщение
     */
    public SendMessage sendTextLoader(Long chatId, String text,List<String> listButtons) {
        try{
            SendMessage sendMessage = new SendMessage(chatId, text);
            sendMessage.replyMarkup(keyboardFactory(listButtons));
            return sendMessage;
        } catch (RuntimeException e) {
            throw new RuntimeException("The list of buttons is invalid");
        }
    }

    @Override
    public SendMessage sendReportNotificationMessage(Long chatId, Long reportId, String text) {
        try{
            SendMessage sendMessage = new SendMessage(chatId, text);
            sendMessage.replyMarkup(keyboardForReportNotificationMessage(reportId));
            return sendMessage;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error on creating message for notification");
        }

    }

    public SendMessage sendTextWithMarkedCallBack(Long chatId, String text, Long reportId) {
        try{
            SendMessage sendMessage = new SendMessage(chatId, text);
            sendMessage.replyMarkup(keyboardForReportAction(reportId));
            return sendMessage;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error on creating message");
        }
    }

    private InlineKeyboardMarkup keyboardForReportNotificationMessage(Long reportId) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Дополнить текст отчета")
                        .callbackData("txt_" + reportId));
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Прислать фотографию для отчета")
                        .callbackData("pic_" + reportId));
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Вернуться в главное меню")
                        .callbackData(getHashFromButton("Вернуться в главное меню")));

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup keyboardForReportAction(Long reportId) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Попросить дополнить текст отчета")
                                .callbackData("txt_" + reportId));
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Попросить прислать фотографию питомца")
                        .callbackData("pic_" + reportId));
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Отметить как прочитанное")
                        .callbackData("oke_" + reportId));
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Назад к списку отчетов")
                        .callbackData(getHashFromButton("Назад к списку отчетов")));
        inlineKeyboardMarkup.addRow(
                new InlineKeyboardButton("Вернуться в меню волонтера")
                        .callbackData(getHashFromButton("Вернуться в меню волонтера")));

        return inlineKeyboardMarkup;
    }


    /**
     * Перегруженный метод, формирующий обновление старого сообщения из входящих параметров:
     * @param update - Из поля update берется id чата, куда будет отправлено сообщение и какое сообщение обновлять
     *               (в данном случае update применяется вместо message, поскольку при нажатии inline кнопки
     *               поле message в update равно null)
     * @param text - текст отправляемого сообщения
     * @param listButtons - список кнопок (текстов кнопок) для клавиатуры
     *                    @see MenuServiceImpl#keyboardFactory(List)
     * @return - возвращает новое сформированное сообщение
     */
    @Override
    public EditMessageText editMenuLoader(Update update, String text, List<String> listButtons) {
        Message message = update.callbackQuery().message();
        Object chatId = message.chat().id();
        int messageId = message.messageId();
        return new EditMessageText(chatId, messageId, text)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .replyMarkup(keyboardFactory(listButtons));
    }

    /**
     * Метод отправки фотографий
     * @param update апдейт из Телеграм
     * @param address путь к файлу
     * @return отправляет фотографию
     */
    @Override
    public SendPhoto sendPhotoLoader(Update update, File address) {
        return new SendPhoto(update.callbackQuery().message().chat().id(), address);
    }

    @Override
    public SendPhoto sendLocationPhotoLoader(Update update, File address, String text, List<String> buttons) {
        SendPhoto sendPhoto = new SendPhoto(update.callbackQuery().message().chat().id(), address);
        sendPhoto.caption(text);
        sendPhoto.replyMarkup(keyboardFactory(buttons));
        return sendPhoto;
    }

    public SendPhoto sendPhotoLoader(Long chatId, File address) {
        return new SendPhoto(chatId, address);
    }

    @Override
    public void multiplePhotoSend(Long chatId, Long reportId) {
        List<ReportPicture> pictures = new ArrayList<>(reportService.getReportPicturesByReportId(reportId));

        try {
            if (pictures.size() != 0) {
                for (ReportPicture pic : pictures) {
                    File file = new File(pic.getFilePath());
                    telegramBot.execute(sendPhotoLoader(chatId, file));
                }
            }
        } catch (Exception e) {
            logger.error("Some exception catched " + e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод для отправки локации - расположения приюта
     * @param update апдейт из Телеграм
     * @param latitude широта
     * @param longitude долгота
     * @return новое сообщение с локацией
     */
    @Override
    public SendLocation sendLocationLoader(Update update, Float latitude, Float longitude) {
        return new SendLocation(update.callbackQuery().message().chat().id(), latitude, longitude);
    }

    /**
     * Метод, генерирующий хэш из строки
     * @param message строка
     * @return хэш в виде строки
     */
    public String getHashFromButton(String message) {
        int hash = Objects.hash(message);
        return Integer.toString(hash);
    }

    /**
     * Метод генерирует список последних отчетов за 3 дня
     * @return список из названий кнопок, состоящий из id отчета и имени юзера и второй элемент - id отчета, который используется как колбек.
     */
    @Override
    public List<List<String>> generateListOfLastReports() {

        List<List<String>> reportButtons = new ArrayList<>();
        List<Report> reportsList = new ArrayList<>(reportService.getUnreadReports());

        if (!reportsList.isEmpty()) {
            for (Report report : reportsList) {
                List<String> button = new ArrayList<>();
                button.add(0, report.getId() + " " + report.getUser().getName() + " Кол-во фото: " + reportService.getNumberOfPicturesByReport(report));
                button.add(1, report.getId().toString());
                reportButtons.add(button);
            }
        }

        return reportButtons;
    }

    /**
     * Метод генерирует список списков, в каждом из которых лежит название кнопки из имени юзера и хэш из id юзера
     * @param name имя, по которому осуществляется поиск
     * @return Список списков
     */
    @Override
    public List<List<String>> generateListOfUsers(String name) {
        List<User> users = userService.getUsersByName(name);
        List<List<String>> namesForKeyboard = new ArrayList<>();
        for (User user : users) {
            List<String> button = new ArrayList<>();
            button.add(user.getName());
            button.add(user.getChatId().toString());
            namesForKeyboard.add(button);
        }

        return namesForKeyboard;

    }

    /**
     * Метод отправляет сообщение с клавиатурой, состоящей из имен пользователей, найденных в базе
     * @param chatId id пользователя
     * @param text сообщение
     * @param name имя, по которому осуществляется поиск
     * @return новое сообщение
     */
    @Override
    public SendMessage sendUserNames(Long chatId, String text, String name) {
        List<List<String>> names = generateListOfUsers(name);

        try{
            SendMessage sendMessage = new SendMessage(chatId, text);
            sendMessage.replyMarkup(keyboardFactoryForObjects(names));
            return sendMessage;
        } catch (RuntimeException e) {
            throw new RuntimeException("The list of buttons is invalid");
        }
    }

}
