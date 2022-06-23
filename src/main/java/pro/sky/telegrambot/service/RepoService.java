package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.User;

public interface RepoService {

    void createUser(Long chatId, String name);

    void setRole(Long chatId);

    User getUserById(Long chatId);

}
