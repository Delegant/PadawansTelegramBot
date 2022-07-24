package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;

import java.io.File;
import java.util.List;

public interface MenuService {

    SendMessage menuLoader (Message message, String text, List<String> listButtons);
    SendMessage menuLoaderForObjects (Message message, String text, List<List<String>> listButtons);
    SendMessage menuLoader (Update update, String text, List<String> listButtons);
    SendMessage menuLoaderForObjects (Update update, String text, List<List<String>> listButtons);
    SendMessage sendTextLoader(Long chatId, String text);
    SendMessage sendTextLoader(Long chatId, String text, List<String> listButtons);
    SendMessage sendTextLoader(Long chatId, String text, List<String> listButtons, List<String> callbacks);
    EditMessageText editMenuLoader(Update update, String text, List<String> listButtons);
    EditMessageText editMenuLoader(Update update, String text, List<String> listButtons, List<String> callBacks);
    EditMessageText editMenuLoader(Update update, String text);
    SendPhoto sendPhotoLoader (Update update, File address);
    SendLocation sendLocationLoader(Update update, Float latitude, Float longitude);

    String getHashFromButton(String message);

    List<String> generateListOfLastReports();

    List<List<String>> generateListOfUsers(String name);
    SendMessage sendUserNames(Long chatId, String text, String name);

}
