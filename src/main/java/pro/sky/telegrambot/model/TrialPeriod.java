package pro.sky.telegrambot.model;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TrialPeriod - класс, описывающий испытательный период для лиц, взявших животное из приюта.
 */
@Schema(description = "Класс, описывающий испытательный период для лиц, взявших животное из приюта.")
@Entity
public class TrialPeriod {

    /**
     * enum состоящий из четырех состояний испытательного периода:
     * STARTED - испытательный период идет
     * ENDED - испытательный период успешно завершен
     * PROLONGED - испытательный период продлен
     * DENIED - испытательный период отклонен
     */
    public enum TrialPeriodStatus {
        STARTED,
        ENDED,
        PROLONGED,
        DENIED,

    }

    @Schema(description = "Идентификатор испытательного периода" )
    @Id
    private Long id;

    @ManyToOne
    @Schema(description = "Идентификатор пользователя-усыновителя")
    private User userId;

    @Schema(description = "Дата начала испытательного периода")
    private LocalDateTime startDate;

    @Schema(description = "Дата завершения испытательного периода")
    private LocalDateTime endDate;

    @Schema(description = "Дополнительные дни для испытательного периода")
    private LocalDateTime additionalDays;

    @Schema(description = "Кем открыт испытательный период")
    private Long setBy;

    @Schema(description = "Кем завершен испытательный период")
    private Long acceptedBy;

    @Schema(description = "Кем продлен испытательный период")
    private Long prolongedBy;

    @Schema(description = "Кем отменен испытательный период")
    private Long deniedBy;

    @Schema(description = "Статус испытательного периода")
    @Enumerated(EnumType.STRING)
    private TrialPeriodStatus status = TrialPeriodStatus.STARTED;

    public TrialPeriod() {
    }

    public TrialPeriod(User userId, Long setBy, TrialPeriodStatus status) {
        this.userId = userId;
        this.setBy = setBy;
        this.status = status;
        this.startDate = LocalDateTime.now();
        this.endDate = startDate.plusDays(30);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getAdditionalDays() {
        return additionalDays;
    }

    public void setAdditionalDays(LocalDateTime additionalDays) {
        this.additionalDays = additionalDays;
    }

    public Long getSetBy() {
        return setBy;
    }

    public void setSetBy(Long setBy) {
        this.setBy = setBy;
    }

    public Long getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(Long acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public Long getProlongedBy() {
        return prolongedBy;
    }

    public void setProlongedBy(Long prolongedBy) {
        this.prolongedBy = prolongedBy;
    }

    public Long getDeniedBy() {
        return deniedBy;
    }

    public void setDeniedBy(Long deniedBy) {
        this.deniedBy = deniedBy;
    }

    public TrialPeriodStatus getStatus() {
        return status;
    }

    public void setStatus(TrialPeriodStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrialPeriod)) return false;
        TrialPeriod that = (TrialPeriod) o;
        return getId().equals(that.getId()) && getUserId().equals(that.getUserId()) && getStartDate().equals(that.getStartDate()) && getEndDate().equals(that.getEndDate()) && Objects.equals(getAdditionalDays(), that.getAdditionalDays()) && Objects.equals(getSetBy(), that.getSetBy()) && Objects.equals(getAcceptedBy(), that.getAcceptedBy()) && Objects.equals(getProlongedBy(), that.getProlongedBy()) && Objects.equals(getDeniedBy(), that.getDeniedBy()) && getStatus() == that.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserId(), getStartDate(), getEndDate(), getAdditionalDays(), getSetBy(), getAcceptedBy(), getProlongedBy(), getDeniedBy(), getStatus());
    }

    @Override
    public String toString() {
        return "TrialPeriod{" +
                "id=" + id +
                ", userId=" + userId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", additionalDays=" + additionalDays +
                ", setBy=" + setBy +
                ", acceptedBy=" + acceptedBy +
                ", prolongedBy=" + prolongedBy +
                ", deniedBy=" + deniedBy +
                ", status=" + status +
                '}';
    }
}
