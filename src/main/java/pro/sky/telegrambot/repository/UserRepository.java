package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByChatId(Long chatId);

    Optional<User> findByName(String name);

//    @Query(value = "SELECT users.chatId from users WHERE role = 'VOLUNTEER'")
    List<User> findAllByRole(String role);

}
