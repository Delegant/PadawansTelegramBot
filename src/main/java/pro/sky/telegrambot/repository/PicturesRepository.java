package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface PicturesRepository extends JpaRepository<ReportPicture, Long> {

    Collection<ReportPicture> findAllByReport(Report report);

    Collection<ReportPicture> findAllByReportId(Long reportId);

    ReportPicture findByFilePathContains(String filename);

    ReportPicture findByFilePathEndingWith(String filename);

    Optional<ReportPicture> findByFilePath(String filename);
}
