package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.telegrambot.model.ReportPicture;

public interface PicturesRepository extends JpaRepository<ReportPicture, Long> {

}
