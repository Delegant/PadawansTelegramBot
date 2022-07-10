package pro.sky.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.Objects;

@Schema(description = "Имена файлов фотографий для отчета")
@Entity(name = "picturenames")
public class PictureName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "file_name")
    private String filename;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "report_id")
    private Report report;

    public PictureName(Long id, String filename) {
        this.id = id;
        this.filename = filename;
    }



    public PictureName() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PictureName)) return false;
        PictureName that = (PictureName) o;
        return getFilename().equals(that.getFilename());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFilename());
    }
}
