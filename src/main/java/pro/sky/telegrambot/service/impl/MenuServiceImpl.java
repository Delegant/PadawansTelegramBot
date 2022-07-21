package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.MenuService;

import static pro.sky.telegrambot.constants.ButtonsText.HIDDEN_BUTTON;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Класс, создающий сообщения с inline-клавиатурой
 * @author Kulachenkov, algmironov
 */
@Service
public class MenuServiceImpl implements MenuService {

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

    @Override
    public SendPhoto sendPhotoLoader(Update update, File address) {
        return new SendPhoto(update.callbackQuery().message().chat().id(), address);
    }

    @Override
    public SendLocation sendLocationLoader(Update update, Float latitude, Float longitude) {
        return new SendLocation(update.callbackQuery().message().chat().id(), latitude, longitude);
    }

    public String getHashFromButton(String message) {
        int hash = Objects.hash(message);
        return Integer.toString(hash);
    }

}
