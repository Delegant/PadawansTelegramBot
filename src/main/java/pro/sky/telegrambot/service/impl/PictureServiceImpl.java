package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pro.sky.telegrambot.model.ReportPicture;
import pro.sky.telegrambot.repository.PicturesRepository;
import pro.sky.telegrambot.service.PictureService;

import java.io.IOException;
import java.util.Comparator;

@Service
public class PictureServiceImpl implements PictureService {

    private final PicturesRepository picturesRepository;

    public PictureServiceImpl(PicturesRepository picturesRepository) {
        this.picturesRepository = picturesRepository;
    }

    @Override
    public ReportPicture savePicture(ReportPicture picture) throws IOException {
        if (picture.getId() == null) {
            if (picturesRepository.findAll().size() != 0) {
                picture.setId(picturesRepository.findAll().stream().max(Comparator.comparing(ReportPicture::getId)).get().getId() + 1);
            }
        }
        return picturesRepository.save(picture);
    }
}
