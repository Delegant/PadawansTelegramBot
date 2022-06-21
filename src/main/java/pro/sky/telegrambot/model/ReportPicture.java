package pro.sky.telegrambot.model;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

@Entity(name = "pictures")
public class ReportPicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long fileSize;

    private String mediaType;

    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    public ReportPicture() {
    }

    public ReportPicture(Long id, long fileSize, String mediaType, byte[] data, Report report) {
        this.id = id;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
        this.data = data;
        this.report = report;
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
                ", mediaType='" + mediaType + '\'' +
                ", data=" + Arrays.toString(data) +
                ", report=" + report +
                '}';
    }
}
