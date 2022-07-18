package pro.sky.telegrambot.Dao.Impl;

import org.springframework.stereotype.Component;
import pro.sky.telegrambot.Dao.Dao;
import pro.sky.telegrambot.model.PictureName;
import pro.sky.telegrambot.model.Report;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class PictureNameDao implements Dao<PictureName> {

    private final EntityManager entityManager;

    public PictureNameDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<PictureName> get(long id) {
        return Optional.ofNullable(entityManager.find(PictureName.class, id));
    }

    @Override
    public List<PictureName> getAll() {
        Query query = entityManager.createQuery("SELECT e FROM picturenames e");

        return query.getResultList();
    }

    @Override
    public void save(PictureName pictureName) {
        executeInsideTransaction(entityManager -> entityManager.persist(pictureName));
    }

    @Override
    public void update(PictureName pictureName, String[] params) {
        pictureName.setFilename(Objects.requireNonNull(params[0], "Filename cannot be null"));
        executeInsideTransaction(entityManager -> entityManager.merge(pictureName));
    }

    @Override
    public void delete(PictureName pictureName) {
        executeInsideTransaction(entityManager -> entityManager.remove(pictureName));
    }

    public List<PictureName> getPictureNamesByReport(Report report) {
        List<PictureName> pictureNames = getAll();

        return pictureNames.stream().filter(pictureName -> pictureName.getReport().equals(report)).collect(Collectors.toList());

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
