package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Message;
import pro.sky.telegrambot.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(Long chatId, String name);

    User updateUser(User user);

    Optional<User> markRole(Long chatId, User.Role role);

    Optional<User> getUserByChatId(Long chatId);

    User getOrCreatUserByMessage(Message message);

    User getOrCreatUserByChatIdAndName(Long chatId, String lastName, String firstName);

    List<User> usersWithEqualRole(User.Role role);

    List<User> getUsersByName(String name);

    User getUserByHashCodeName(String data);

    void setCompanion(User volunteer, User user);

    void delCompanion(User volunteer, User user);

}
