package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.User;

import java.util.Optional;

public interface RepoService {

    void createUser(Long chatId, String name);

    void markRole(Long chatId, User.Role role);

    Optional<User> getUserById(Long chatId);

}
