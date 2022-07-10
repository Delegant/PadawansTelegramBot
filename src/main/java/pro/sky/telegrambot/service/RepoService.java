package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.User;

import java.util.Optional;

public interface RepoService {

    User createUser(Long chatId, String name);

    User markRole(Long chatId, User.Role role);

    Optional<User> getUserById(Long chatId);



}
