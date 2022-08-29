package pro.sky.telegrambot.repository;

import liquibase.repackaged.net.sf.jsqlparser.statement.select.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.TrialPeriod;
import pro.sky.telegrambot.model.User;

import java.util.Collection;

@Repository
public interface TrialPeriodRepository extends JpaRepository<TrialPeriod, Long> {
    TrialPeriod findByUser(User user);

    TrialPeriod getTrialPeriodById(Long id);

    @Query(value = "SELECT * FROM trial_periods", nativeQuery = true)
    public Collection<TrialPeriod> getAll();
    Collection<TrialPeriod> findByUserChatId(Long userId);

    @Query(value = "SELECT user_id from trial_periods where id = :trialPeriodId", nativeQuery = true)
    Long getUserId(Long trialPeriodId);
}
