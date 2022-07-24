package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.Dao.Impl.UserDao;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.UserService;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final UserDao userDao;

    public UserServiceImpl(UserRepository userRepository,
                           UserDao userDao) {
        this.userRepository = userRepository;
        this.userDao = userDao;
    }

    public User createUser(Long chatId, String name) {
        logger.info("==== Processing create user: {}, {}", chatId, name);
        User user = new User(chatId, name);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
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
    public User getOrCreatUserByMessage(Message message) {
            Long chatId = message.chat().id();
            String lastName = message.chat().lastName();
            String firstName = message.chat().firstName();
            return getUserByChatId(chatId).orElseGet(() -> createUser(chatId, lastName + " " + firstName));
    }

    /**
     * Метод, проверяющий наличие пользователя в базе и возвращающий пользователя
     * для дальнейшей работы. Если пользователя в базе нет - создает и сохраняет нового - USER
     *
     * @return USER - не может быть null
     * @see User
     */
    @Override
    public User getOrCreatUserByChatIdAndName(Long chatId, String lastName, String firstName) {
        return getUserByChatId(chatId).orElseGet(() -> createUser(chatId, lastName + " " + firstName));
    }

    /**
     * Метод возвращает пользователя
     * @param chatId id пользователя
     * @return юзер
     * @throws UserNotFoundException если юзер не найден
     */
    public User getUser(Long chatId) {
        return getUserByChatId(chatId)
                .orElseThrow(() -> new UserNotFoundException("!!!! User with such id not found!"));
    }

    /**
     * Возвращает список пользователей, у которых роль совпадает с заданной
     * @param role роль
     * @return список юзеров
     */
    @Override
    public List<User> usersWithEqualRole(User.Role role) {
        return userRepository.findAllByRole(role);
    }

    /**
     * Метод возвращает из репозитория список юзеров у которых имеются совпадения с заданным именем
     * @param name имя по которому осуществляется поиск
     * @return список юзеров
     */
    @Override
    public List<User> getUsersByName(String name) {
        return userRepository.findAllByNameContainsIgnoreCase(name);
    }

    /**
     * Возвращает юзера, у которого совпадает хэшкод имени с полученным от Телеграм
     * @param data входящие данные
     * @return Пользователя
     */
    @Deprecated
    @Override
    public User getUserByHashCodeName(String data) {
        Map<Integer, User> hashCodes = new HashMap<>();
        List<User> users = userDao.getAll();
        Optional<User> targetUser = Optional.empty();
        for (User user : users) {
            hashCodes.put(user.getName().hashCode(), user);
            if (hashCodes.containsKey(Integer.parseInt(data))) {
                targetUser = Optional.of(hashCodes.get(Integer.parseInt(data)));
            }
        }
        return targetUser.orElseThrow(() -> new UserNotFoundException("!!!! There is no user found with such hashCode from name"));
    }

    /**
     * Проставляем номера чат ID для юзера и волонтера, что бы зеркалить общение
     * @param volunteer - объект User, где отражаеться волонтер, который ответил на призыв
     * @param user - объект User запросивший помощь
     */
    @Override
    public void setCompanion(User volunteer, User user) {
        volunteer.setCompanion(user.getChatId());
        user.setCompanion(volunteer.getChatId());
        userRepository.saveAll(List.of(volunteer, user));
    }
    /**
     * Проставляем номера чат ID для юзера и волонтера, что бы зеркалить общение
     * @param volunteer - объект User, где отражаеться волонтер, который ответил на призыв
     * @param user - объект User запросивший помощь
     */
    @Override
    public void delCompanion(User volunteer, User user) {
        volunteer.setCompanion(null);
        user.setCompanion(null);
        userRepository.saveAll(List.of(volunteer, user));
    }
}
