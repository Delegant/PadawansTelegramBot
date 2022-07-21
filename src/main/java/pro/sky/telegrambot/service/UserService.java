package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Message;
import pro.sky.telegrambot.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(Long chatId, String name);

    Optional<User> markRole(Long chatId, User.Role role);

    Optional<User> getUserByChatId(Long chatId);

    User getUserByMessage(Message message);

    List<User> usersWithEqualRole(User.Role role);

}
