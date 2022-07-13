package pro.sky.telegrambot.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.service.ReportService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static pro.sky.telegrambot.constants.ReportControllerConstants.*;
import static pro.sky.telegrambot.constants.UserServiceConstants.EXPECTED_ID;


@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    HttpServletResponse httpServletResponse;

    @Mock
    ReportService reportService;

    @InjectMocks
    ReportController out;

    @Test
    void shouldCallSaveReportInServiceWhenRunSaveReport() {
        out.saveReport(reportForTest);
        Mockito.verify(reportService).saveReport(EXPECTED_ID, SOME_REPORT_TEXT);
    }

    @Test
    void shouldCallServiceSavePicturesWhenSaveReportPictures() throws IOException {
        out.saveReportPictures(EXPECTED_ID, files);
        Mockito.verify(reportService).savePictures(EXPECTED_ID, files);
    }

    @Test
    void shouldCallGetReportPicturesByReportIdWhenGetPicturesOfReport() throws IOException {
        out.getPicturesOfReport(EXPECTED_ID, httpServletResponse);
        Mockito.verify(reportService).getReportPicturesByReportId(EXPECTED_ID);
    }

}