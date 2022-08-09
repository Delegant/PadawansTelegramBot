package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import pro.sky.telegrambot.model.PictureName;
import pro.sky.telegrambot.model.User;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface MenuService {

    SendMessage menuLoader (Message message, String text, List<String> listButtons);
    SendMessage menuLoaderForObjects (Message message, String text, List<List<String>> listButtons);
    SendMessage menuLoader (Update update, String text, List<String> listButtons);
    SendMessage menuLoaderForObjects (Update update, String text, List<List<String>> listButtons);

    InlineKeyboardMarkup getMainKeyboard();
    SendMessage sendTextLoader(Long chatId, String text);
    SendMessage sendTextLoader(Long chatId, String text, List<String> listButtons);
    @Deprecated
    SendMessage replyKeyboardLoader(List<String> list, String text, Message message);
    @Deprecated
    SendMessage replyKeyboardLoader(List<String> list, String text, Update update);
    EditMessageText editMenuLoader(Update update, String text, List<String> listButtons);
    SendPhoto sendLocationPhotoLoader (Update update, File address, String text, List<String> buttons);
    SendPhoto sendPhotoLoader (Update update, File address);
    SendPhoto sendPhotoLoader (Long chatId, File address);
    void multiplePhotoSend(Long chatId, Long reportId);
    SendMessage sendTextWithMarkedCallBack(User user, String text, Long reportId);

    SendMessage sendTrialPeriod(User user, Long trialPeriodId);
    SendLocation sendLocationLoader(Update update, Float latitude, Float longitude);
    String getHashFromButton(String message);
    List<List<String>> generateListOfLastReports();
    List<List<String>> generateListOfAllUserReports(Long chatId);
    List<List<String>> generateListOfAllTrialPeriods();

    List<List<String>> generateListOfUpdateRequestedUserReports(Long chatId);

    SendMessage sendUserNames(Long chatId, String text, String name);

    SendMessage sendReportNotificationMessage(Long chatId, Long reportId, String text);
    List<List<String>> generateListOfUsers(String name);

    SendMessage sendTextLoader(Long volunteer_chatId, String volunteer_request_text, List<String> to_support_menu, List<String> user_chatId);
}
