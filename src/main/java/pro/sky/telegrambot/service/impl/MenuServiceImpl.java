package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.MenuService;

import java.util.List;
import java.util.Objects;

/**
 * Класс, создающий сообщения с inline-клавиатурой
 *
 * @author Kulachenkov, algmironov
 */
@Service
public class MenuServiceImpl implements MenuService {

    /**
     * Метод, принимающий список кнопок и формирующий клавиатуру для вставки в сообщение.
     *
     * @param list - входящий список кнопок (текстов для кнопок)
     * @return - возвращает inline-клавиатуру
     * @see MenuServiceImpl#menuLoader(Message, String, List)
     * @see MenuServiceImpl#menuLoader(Update, String, List)
     */
    private InlineKeyboardMarkup keyboardFactory(List<String> list) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        if (list.size() <= 10) {
            for (int i = 0; i < list.size(); i++) {
                inlineKeyboardMarkup.addRow(new InlineKeyboardButton(list.get(i)).callbackData(hashFromButton(list.get(i))));
            }
        }
        if (list.size() > 10 && list.size() % 2 == 0) {
            for (int i = 0; i < list.size(); i = i + 2) {
                inlineKeyboardMarkup.addRow(new InlineKeyboardButton(list.get(i))
                                .callbackData(hashFromButton(list.get(i))),
                        new InlineKeyboardButton(list.get(i + 1))
                                .callbackData(hashFromButton(list.get(i + 1))));
            }
        }
        if (list.size() > 10 && list.size() % 2 != 0) {
            for (int i = 0; i < list.size() - 1; i = i + 2) {
                inlineKeyboardMarkup.addRow(new InlineKeyboardButton(list.get(i))
                                .callbackData(hashFromButton(list.get(i))),
                        new InlineKeyboardButton(list.get(i + 1))
                                .callbackData(hashFromButton(list.get(i + 1))));
            }
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(list.get(list.size() - 1))
                    .callbackData(hashFromButton(list.get(list.size() - 1))));
        }


        return inlineKeyboardMarkup;
    }

    /**
     * Метод, формирующий новое сообщение из входящих параметров:
     *
     * @param message     - Из поля message берется id чата, куда будет отправлено сообщение
     * @param text        - текст отправляемого сообщения
     * @param listButtons - список кнопок (текстов кнопок) для клавиатуры
     * @return - возвращает новое сформированное сообщение
     * @see MenuServiceImpl#keyboardFactory(List)
     */
    @Override
    public SendMessage menuLoader(Message message, String text, List<String> listButtons) {
        Keyboard keyboard = keyboardFactory(listButtons);
        return new SendMessage(message.chat().id(), text)
                .replyMarkup(keyboard);
    }

    /**
     * Перегруженный метод, формирующий новое сообщение из входящих параметров:
     *
     * @param update      - Из поля update берется id чата, куда будет отправлено сообщение
     *                    (в данном случае update применяется вместо message, поскольку при нажатии inline кнопки
     *                    поле message в update равно null)
     * @param text        - текст отправляемого сообщения
     * @param listButtons - список кнопок (текстов кнопок) для клавиатуры
     * @return - возвращает новое сформированное сообщение
     * @see MenuServiceImpl#keyboardFactory(List)
     */
    public SendMessage menuLoader(Update update, String text, List<String> listButtons) {
        Keyboard keyboard = keyboardFactory(listButtons);
        return new SendMessage(update.callbackQuery().message().chat().id(), text)
                .replyMarkup(keyboard);
    }

    @Override
    public EditMessageText editMenu(Update update, String text, List<String> listButtons) {
        Message message = update.callbackQuery().message();
        Object chatId = message.chat().id();
        int messageId = message.messageId();
        return new EditMessageText(chatId, messageId, text)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .replyMarkup(keyboardFactory(listButtons));
    }

    public String hashFromButton(String message) {
        int hash = Objects.hash(message);
        return Integer.toString(hash);
    }

}
