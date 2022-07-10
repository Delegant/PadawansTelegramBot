package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.User;

import java.util.Optional;

public interface RepoService {

    User createUser(Long chatId, String name);

    Optional<User> markRole(Long chatId, User.Role role);




}
