package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.User;

import java.util.Optional;

@Repository
public interface MenuStackRepository extends JpaRepository<MenuStack, Long> {

    Optional<MenuStack> findTopByUserOrderByIdDesc(User user);

    @Query(value = "SELECT m.* FROM users u, menustack m WHERE u.id = :user ORDER BY m.id DESC OFFSET 1 LIMIT 1", nativeQuery = true)
    Optional<MenuStack> findLastMenuStateByUser(User user);

}
