package pro.sky.telegrambot.Dao.Impl;

import org.springframework.stereotype.Component;
import pro.sky.telegrambot.Dao.Dao;
import pro.sky.telegrambot.model.PictureName;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class ReportDao implements Dao<Report> {

    private final EntityManager entityManager;

    public ReportDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Report> get(long id) {
        return Optional.ofNullable(entityManager.find(Report.class, id));
    }

    @Override
    public List<Report> getAll() {
        Query query = entityManager.createQuery("SELECT e FROM reports e");

        return query.getResultList();
    }

    @Override
    public void save(Report report) {
        executeInsideTransaction(entityManager -> entityManager.persist(report));
    }

    @Override
    public void update(Report report, String[] params) {
        report.setReportText(Objects.requireNonNull(params[0], "Report text cannot be null"));
        executeInsideTransaction(entityManager -> entityManager.merge(report));
    }

    public void setPictureNames(Report report, List<PictureName> pictureNames) {
        report.setPictureNames(pictureNames);
        executeInsideTransaction(entityManager -> entityManager.merge(report));
    }

    public void setPicturesOfReport(Report report, Collection<ReportPicture> pictures) {
        report.setPicturesOfReport(pictures);
        executeInsideTransaction(entityManager -> entityManager.merge(report));
    }

    @Override
    public void delete(Report report) {
        executeInsideTransaction(entityManager -> entityManager.remove(report));
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
