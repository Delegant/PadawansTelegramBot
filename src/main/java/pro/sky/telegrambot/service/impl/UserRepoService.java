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

    public void createUser(Long chatId, String name) {
        User user = new User(chatId, name);
        userRepository.save(user);
    }

    public void markRole(Long chatId, User.Role role) {
        getUserById(chatId).ifPresent(u -> {
            u.setRole(role);
            userRepository.save(u);
        });
    }

    public Optional<User> getUserById(Long chatId){
        return userRepository.findUserByChatId(chatId);
    }

}
