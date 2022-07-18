package pro.sky.telegrambot.constants;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;
import java.util.Collection;
import java.util.List;

import static pro.sky.telegrambot.constants.UserServiceConstants.*;

public class ReportControllerConstants {

    public static final Report reportForTest = new Report();
    public static final String SOME_REPORT_TEXT = "someReportText";
    public static final String FILE_NAME = "someReportText";
    public static final byte[] CONTENT = {1,2,3,4,5,6};

    public static final List<MultipartFile> files = List.of(new MockMultipartFile(FILE_NAME, CONTENT));
    public static final Collection<ReportPicture> reportPictures = List.of(new ReportPicture());
    static {
        reportForTest.setUser(userFromDataBase);
        reportForTest.setReportText(SOME_REPORT_TEXT);
        reportForTest.setReportDate();
        reportForTest.setId(EXPECTED_ID);
        reportForTest.setStatus(Report.Status.UPDATED);
    }

}