package pro.sky.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Fetch;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Schema(description = "Отчет пользователя")
@Entity(name = "reports")
@NamedQueries({
        @NamedQuery(name = "Report.findByReportDateIsBetween", query = "select r from reports r where r.reportDate between :reportDate and :reportDateEnd")
})
public class Report {

    public enum Status {
        MAIN,
        UPDATED,
    }

    public enum ReadStatus {
        UNREAD,
        READ,
        TO_BE_UPDATED,
    }

    @Schema(description = "Идентификатор отчета")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Текст отчета")
    private String reportText;

    @Schema(description = "Дата отчета")
    private LocalDateTime reportDate;

    @Schema(description = "Дата обновления отчета")
    private LocalDateTime reportUpdateDate;

    @Schema(description = "Пользователь - автор отчета")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Schema(description = "Статус отчета: основной/дополненный")
    @Enumerated(EnumType.STRING)
    private Status status = Status.MAIN;

    @Schema(description = "Статус проверки отчета: не проверен/проверен/требует доработки")
    @Enumerated(EnumType.STRING)
    private ReadStatus readStatus = ReadStatus.UNREAD;

    @Schema(description = "Фотографии для отчета")
    @JsonIgnore
    @OneToMany(mappedBy = "report")
    private Collection<ReportPicture> picturesOfReport;

    @Schema(description = "Set названий картинок")
    @JsonIgnore
    @OneToMany(mappedBy = "filename")
    private List<PictureName> pictureNames;

    public Report() {
    }

    public Report(Long id, String reportText) {
        this.id = id;
        this.reportText = reportText;
        this.reportDate = LocalDateTime.now();
        this.status = Status.MAIN;
        this.readStatus = ReadStatus.UNREAD;
    }

    public Report(Long id, String reportText, Status status) {
        this.id = id;
        this.reportText = reportText;
        this.reportDate = LocalDateTime.now();
        this.status = status;
    }

    public List<PictureName> getPictureNames() {
        return pictureNames;
    }

    public ReadStatus getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(ReadStatus readStatus) {
        this.readStatus = readStatus;
    }

    @Transactional
    public void setPictureNames(List<PictureName> pictureNames) {
        this.pictureNames = pictureNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public LocalDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate() {
        this.reportDate = LocalDateTime.now();
    }
    public void setReportDate(LocalDateTime date) {
        this.reportDate = date;
    }

    public LocalDateTime getReportUpdateDate() {
        return reportUpdateDate;
    }

    public void setReportUpdateDate() {
        this.reportUpdateDate = LocalDateTime.now();
    }

    @Transactional
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDefaultStatus() {
        this.status = Status.MAIN;
    }

    public Collection<ReportPicture> getPicturesOfReport() {
        return picturesOfReport;
    }

    public void setPicturesOfReport(Collection<ReportPicture> picturesOfReport) {
        this.picturesOfReport = picturesOfReport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Report)) return false;
        Report report = (Report) o;
        return id.equals(report.id) && reportText.equals(report.reportText) && reportDate.equals(report.reportDate) && Objects.equals(reportUpdateDate, report.reportUpdateDate) && user.equals(report.user) && status == report.status && Objects.equals(picturesOfReport, report.picturesOfReport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reportText, reportDate, reportUpdateDate, user, status, picturesOfReport);
    }

    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", reportText='" + reportText + '\'' +
                ", reportDate=" + reportDate +
                ", reportUpdateDate=" + reportUpdateDate +
                ", user=" + user +
                ", status=" + status +
                '}';
    }
}
