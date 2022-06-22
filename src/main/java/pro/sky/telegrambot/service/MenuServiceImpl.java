package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MenuServiceImpl implements MenuService{

    private InlineKeyboardMarkup keyboardFactory(List<String> list) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        for (String s : list) {
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(s).callbackData(s));
        }
        return inlineKeyboardMarkup;
    }

    @Override
    public SendMessage menuLoader (Message message, String text, List<String> listButtons) {
        Keyboard keyboard = keyboardFactory(listButtons);
        return new SendMessage(message.chat().id(), text)
                .replyMarkup(keyboard);

    }
}
