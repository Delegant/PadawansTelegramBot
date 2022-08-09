package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.User;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Repository
public interface ReportsRepository extends JpaRepository<Report, Long> {

    Collection<Report> findAllByUserId(Long userId);

    Collection<Report> findAllByUserChatId(Long chatId);

    Collection<Report> findAllByUser(User user);

    @Override
    boolean existsById(@NonNull Long reportId);

}
