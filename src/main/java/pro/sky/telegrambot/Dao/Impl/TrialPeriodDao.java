package pro.sky.telegrambot.Dao.Impl;

import liquibase.pro.packaged.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.Dao.Dao;
import pro.sky.telegrambot.model.TrialPeriod;
import pro.sky.telegrambot.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class TrialPeriodDao implements Dao<TrialPeriod> {

    private final EntityManager entityManager;

    @Autowired
    private UserDao userDao;

    public TrialPeriodDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<TrialPeriod> get(long id) {
        return Optional.of(entityManager.find(TrialPeriod.class, id));
    }

    @Override
    public List<TrialPeriod> getAll() {
        return entityManager.createQuery("SELECT e FROM trial_periods e").getResultList();
    }

    @Override
    public void save(TrialPeriod trialPeriod) {
        executeInsideTransaction(entityManager -> entityManager.persist(trialPeriod));
    }

    @Override
    public void update(TrialPeriod trialPeriod, String[] params) {

    }

    @Override
    public void delete(TrialPeriod trialPeriod) {
        executeInsideTransaction(entityManager -> entityManager.remove(trialPeriod));
    }

    @Transactional
    public User getUser(TrialPeriod period) {
        List<TrialPeriod> all = getAll();
        User user = null;
        for (TrialPeriod tp : all) {
            if (tp.equals(period)) {
                var userId = tp.getUserId().getId();
                user = userDao.get(userId).orElseThrow();
            }
        }

        return user;
    }

    public User getUserFromTrialPeriod(TrialPeriod trialPeriod) {
        return trialPeriod.getUserId();
    }

    private void executeInsideTransaction(Consumer<EntityManager> action) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            action.accept(entityManager);
            tx.commit();
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }
}
