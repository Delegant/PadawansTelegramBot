package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.UserService;

import java.util.Optional;

@Service
public class UserServiceImpl implements pro.sky.telegrambot.service.UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(Long chatId, String name) {
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

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

}
