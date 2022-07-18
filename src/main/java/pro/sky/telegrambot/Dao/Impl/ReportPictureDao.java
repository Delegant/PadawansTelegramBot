package pro.sky.telegrambot.Dao.Impl;

import org.springframework.stereotype.Component;
import pro.sky.telegrambot.Dao.Dao;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class ReportPictureDao implements Dao<ReportPicture> {


    private final EntityManager entityManager;

    public ReportPictureDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<ReportPicture> get(long id) {
        return Optional.ofNullable(entityManager.find(ReportPicture.class, id));
    }

    @Override
    public List<ReportPicture> getAll() {
        Query query = entityManager.createQuery("SELECT e FROM pictures e");

        return query.getResultList();
    }

    @Override
    public void save(ReportPicture reportPicture) {
        executeInsideTransaction(entityManager -> entityManager.persist(reportPicture));
    }

    @Override
    public void update(ReportPicture reportPicture, String[] params) {
        reportPicture.setFilePath(Objects.requireNonNull(params[0], "FilePath cannot be null"));
        executeInsideTransaction(entityManager -> entityManager.merge(reportPicture));
    }

    @Override
    public void delete(ReportPicture reportPicture) {
        executeInsideTransaction(entityManager -> entityManager.remove(reportPicture));
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

    public Collection<ReportPicture> getReportPicturesByReport(Report report) {
        Collection<ReportPicture> pictures = getAll();
        return pictures.stream().filter(picture -> picture.getReport().equals(report)).collect(Collectors.toList());
    }
}
