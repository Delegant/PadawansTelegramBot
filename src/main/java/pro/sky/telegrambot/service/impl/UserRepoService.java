package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.RepoService;

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

    public void setRole(Long chatId) {
       User user = getUserById(chatId);
       user.setRole(User.Role.USER);
    }

    public User getUserById(Long chatId){
        return userRepository.getByChatId(chatId);
    }

}
