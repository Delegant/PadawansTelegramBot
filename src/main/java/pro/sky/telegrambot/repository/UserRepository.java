package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByChatId(Long chatId);

    Optional<User> findByName(String name);
    List<User> findAllByRole(User.Role role);

    List<User> findAllByNameContainsIgnoreCase(String name);

}
