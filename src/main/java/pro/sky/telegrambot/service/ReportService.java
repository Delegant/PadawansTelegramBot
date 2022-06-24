package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.repository.ReportsRepository;

import java.util.Collection;

public interface ReportService {

    void saveReport(Long userId, String reportText);

    Collection<Report> getReportsFromParent(Long userId);




}
