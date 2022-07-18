package pro.sky.telegrambot.service;

import org.springframework.web.multipart.MultipartFile;
import pro.sky.telegrambot.model.ReportPicture;

import java.io.IOException;

public interface PictureService {

    ReportPicture savePicture(ReportPicture picture) throws IOException;

}
