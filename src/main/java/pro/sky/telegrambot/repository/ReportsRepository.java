package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.User;

import java.util.Collection;

@Repository
public interface ReportsRepository extends JpaRepository<Report, Long> {

    Collection<Report> findAllByUser(Long userId);

    Collection<Report> findAllByUser(User user);

}
