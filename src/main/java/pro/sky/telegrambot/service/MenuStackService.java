package pro.sky.telegrambot.service;

public interface MenuStackService {

    String getTextPackKey(Long chatId);
    String getLastMenuState(Long chatId);


}
