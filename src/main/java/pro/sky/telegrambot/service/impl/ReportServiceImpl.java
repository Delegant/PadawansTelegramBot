package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.repository.ReportsRepository;
import pro.sky.telegrambot.repository.UserRepository;
import pro.sky.telegrambot.service.ReportService;

import java.util.Collection;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportsRepository reportsRepository;
    private final PicturesRepository picturesRepository;

    private final UserRepository userRepository;

    public ReportServiceImpl(ReportsRepository reportsRepository, PicturesRepository picturesRepository, UserRepository userRepository) {
        this.reportsRepository = reportsRepository;
        this.picturesRepository = picturesRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void saveReport(Long userId, String reportText) {
        Report report = new Report();
        report.setReportText(reportText);
        report.setUser(userRepository.findUserById(userId));
        report.setReportDate();
        reportsRepository.save(report);
    }

    @Override
    public Collection<Report> getReportsFromParent(Long userId) {
        return reportsRepository.findAllByUserId(userId);
    }
}
