package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.MenuStack;
import pro.sky.telegrambot.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuStackRepository extends JpaRepository<MenuStack, Long> {

    Optional<MenuStack> findTopByUserOrderByIdDesc(User user);

    @Query(value = "select mm.* from users u INNER JOIN LATERAl (select m.* from menustack m where m.user_id=u.id order by m.id desc limit 1) mm on u.id=mm.user_id and u.role like :#{#role?.name()}", nativeQuery = true)
    List<MenuStack> findAllLastMenuStackByUserRole(User.Role role);

}
