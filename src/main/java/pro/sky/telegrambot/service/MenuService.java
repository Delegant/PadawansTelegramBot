package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.List;

public interface MenuService {

    SendMessage menuLoader (Message message, String text, List<String> listButtons);

}
