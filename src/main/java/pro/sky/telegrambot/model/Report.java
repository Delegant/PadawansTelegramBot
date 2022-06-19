package pro.sky.telegrambot.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;

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
}
