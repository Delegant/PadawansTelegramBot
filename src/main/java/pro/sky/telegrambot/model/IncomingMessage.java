package pro.sky.telegrambot.model;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Schema(description = "Входящие сообщения от пользователей")
@Entity(name = "messages")
public class IncomingMessage {

    private enum ReadStatus {
        UNREAD,
        READ,
    }

    @Schema(description = "Идентификатор сообщения")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @Schema(description = "Идентификатор отправителя")
    private Long senderId;

    @Schema(description = "Текст сообщения")
    private String messageText;

    @Schema(description = "Время отправки сообщения")
    private LocalDateTime sentDate;

    @Schema(description = "Статус сообщения: прочитано/не прочитано")
    @Enumerated(EnumType.STRING)
    private ReadStatus readStatus = ReadStatus.UNREAD;


    public IncomingMessage() {
    }

    public IncomingMessage(Long senderId, String messageText) {
        this.senderId = senderId;
        this.messageText = messageText;
        this.sentDate = LocalDateTime.now();
        this.readStatus = ReadStatus.UNREAD;
    }

    public ReadStatus getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(ReadStatus readStatus) {
        this.readStatus = readStatus;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncomingMessage)) return false;
        IncomingMessage that = (IncomingMessage) o;
        return getSenderId().equals(that.getSenderId()) && getMessageText().equals(that.getMessageText()) && getSentDate().equals(that.getSentDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSenderId(), getMessageText(), getSentDate());
    }

    @Override
    public String toString() {
        return "IncomingMessage{" +
                "senderId=" + senderId +
                ", messageText='" + messageText + '\'' +
                ", sentDate=" + sentDate +
                '}';
    }
}
