package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.MenuStack;

import java.util.Optional;

@Repository
public interface MenuStackRepository extends JpaRepository<MenuStack, Long> {

    @Query("SELECT m.textPackageKey FROM MenuStack m WHERE m.user.chatId = :chatId")
    Optional<String> findTextPackageKeyByChatId(Long chatId);

    @Query("SELECT m.menuState FROM MenuStack m WHERE m.user.chatId = :chatId")
    Optional<String> findMenuStateChatId(Long chatId);

}
