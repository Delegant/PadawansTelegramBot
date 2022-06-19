package pro.sky.telegrambot.model;

import javax.persistence.*;

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

}
