package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.PictureName;

import java.util.Collection;

@Repository
public interface PictureNameRepository extends JpaRepository<PictureName, Long> {
    public Collection<PictureName> findAllByReportId(Long reportId);
}
