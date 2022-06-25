package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


}
