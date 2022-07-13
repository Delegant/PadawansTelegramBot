package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.TrialPeriod;

@Repository
public interface TrialPeriodRepository extends JpaRepository<TrialPeriod, Long> {
    public TrialPeriod findByUserId(Long userId);
}
