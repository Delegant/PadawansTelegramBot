package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements pro.sky.telegrambot.service.UserService {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(Long chatId, String name) {
        logger.info("==== Processing create user: {}, {}", chatId, name);
        User user = new User(chatId, name);
        return userRepository.save(user);
    }

    public Optional<User> markRole(Long chatId, User.Role role) {
        Optional<User> optionalUser = getUserByChatId(chatId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRole(role);
            return Optional.of(userRepository.save(user));
        }
        return optionalUser;
    }

    @Override
    public Optional<User> getUserByChatId(Long chatId) {
        return userRepository.findUserByChatId(chatId);
    }

    /**
     * Метод, проверяющий наличие пользователя в базе и возвращающий пользователя
     * для дальнейшей работы. Если пользователя в базе нет - создает и сохраняет нового - USER
     *
     * @param message сообщение из обновления
     * @return USER - не может быть null
     * @see User
     */
    @Override
    public User getUserByMessage(Message message) {
            Long chatId = message.chat().id();
            String lastName = message.chat().lastName();
            String firstName = message.chat().firstName();
            return getUserByChatId(chatId).orElseGet(() -> createUser(chatId, lastName + " " + firstName));
    }

    @Override
    public List<User> usersWithEqualRole(User.Role role) {
        return userRepository.findAllByRole(role);
    }

}
