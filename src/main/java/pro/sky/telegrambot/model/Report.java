package pro.sky.telegrambot.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@Entity(name = "reports")
public class Report {

    private enum Status {
        MAIN,
        UPDATED,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportText;

    private LocalDateTime reportDate;

    private LocalDateTime reportUpdateDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Status status = Status.MAIN;

    @OneToMany(mappedBy = "report")
    private Collection<ReportPicture> picturesOfReport;

    public Report() {
    }

    public Report(Long id, String reportText, Status status) {
        this.id = id;
        this.reportText = reportText;
        this.reportDate = LocalDateTime.now();
        this.status = status;
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

    public LocalDateTime getReportUpdateDate() {
        return reportUpdateDate;
    }

    public void setReportUpdateDate() {
        this.reportUpdateDate = LocalDateTime.now();
    }

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
                ", picturesOfReport=" + picturesOfReport +
                '}';
    }
}
