package pro.sky.telegrambot.Dao.Impl;

import org.springframework.stereotype.Component;
import pro.sky.telegrambot.Dao.Dao;
import pro.sky.telegrambot.model.PictureName;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;
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

    public int getNumberOfPicturesByReport(Report report) {
        List<PictureName> names = entityManager.createQuery("SELECT p from picturenames p").getResultList();
        int number = 0;
        for (PictureName name : names) {
            if (name.getReport().equals(report)) {
                number++;
            }
        }
        return number;
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

    public Collection<Report> getUnreadReports() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime minusThree = LocalDateTime.now().minusDays(3);

        Query query = entityManager.createQuery("Select r from reports  r");
        Collection<Report> reports = new ArrayList<>();
        List<Report> resultList= query.getResultList();
        for (int i = 0; i < resultList.size(); i++) {
            if (resultList.get(i).getReportDate().isAfter(minusThree)) {
                reports.add(resultList.get(i));
            }
        }
        return reports;
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
