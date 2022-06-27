package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.RepoService;

import java.util.Optional;

@Service
public class UserRepoService implements RepoService {

    private final UserRepository userRepository;

    public UserRepoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(Long chatId, String name) {
        User user = new User(chatId, name);
        return userRepository.save(user);
    }

    public User markRole(Long chatId, User.Role role) {
        Optional<User> user = getUserById(chatId);
        User result = null;
        if (user.isPresent()) {
            user.get().setRole(role);
            result = userRepository.save(user.get());
        }
        return result;
    }

    public Optional<User> getUserById(Long chatId){
        return userRepository.findUserByChatId(chatId);
    }

}
