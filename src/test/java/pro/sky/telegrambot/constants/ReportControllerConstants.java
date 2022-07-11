package pro.sky.telegrambot.constants;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import pro.sky.telegrambot.model.Report;
import pro.sky.telegrambot.model.ReportPicture;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

import static pro.sky.telegrambot.constants.UserServiceConstants.*;

public class ReportControllerConstants {

    public static final Report report = new Report();
    public static final String SOME_REPORT_TEXT = "someReportText";
    public static final String FILE_NAME = "someReportText";
    public static final byte[] CONTENT = {1,2,3,4,5,6};

    public static final List<MultipartFile> files = List.of(new MockMultipartFile(FILE_NAME, CONTENT));
    public static final Collection<ReportPicture> reportPictures = List.of(new ReportPicture());
    static {
        report.setUser(userFromDataBase);
        report.setReportText(SOME_REPORT_TEXT);
    }

}
