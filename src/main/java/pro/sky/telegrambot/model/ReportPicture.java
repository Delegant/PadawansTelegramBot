package pro.sky.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

@Schema(description = "Фотография для отчета")
@Entity(name = "pictures")
public class ReportPicture {

    @Schema(description = "Идентификатор фотографии для отчета")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Размер файла")
    private long fileSize;

    @Schema(description = "Путь к файлу (локальный)")
    private String filePath;

    @Schema(description = "Тип файла")
    private String mediaType;

    @Schema(description = "Массив байтов - файл в байтовом виде")
    private byte[] data;

    @Schema(description = "Идентификатор отчета к которому привязана фотография")
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "report_id")
    private Report report;

    public ReportPicture() {
    }

    public ReportPicture(Long id, long fileSize, String filePath, String mediaType, byte[] data, Report report) {
        this.id = id;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.mediaType = mediaType;
        this.data = data;
        this.report = report;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
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
        if (!(o instanceof ReportPicture)) return false;
        ReportPicture that = (ReportPicture) o;
        return getFileSize() == that.getFileSize() && getId().equals(that.getId()) && getMediaType().equals(that.getMediaType()) && Arrays.equals(getData(), that.getData()) && getReport().equals(that.getReport());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getFileSize(), getMediaType(), getReport());
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }

    @Override
    public String toString() {
        return "ReportPicture{" +
                "id=" + id +
                ", fileSize=" + fileSize +
                ", filePath='" + filePath + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
