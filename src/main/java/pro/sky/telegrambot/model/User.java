package pro.sky.telegrambot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Schema(description = "Пользователь")
@Entity(name = "users")
public class User {

    public enum Role {
        USER,
        PARENT,
        VOLUNTEER,
        ADMIN,
        CHANNEL,
    }

    @Schema(description = "Идентификатор пользователя")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Идентификатор пользователя в Телеграм")
//    @Column(name = "chat_id")
    private Long chatId;

    @Schema(description = "Имя пользователя")
    private String name;

    @Schema(description = "'Роль' пользователя")
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Schema(description = "Отчеты пользователя")
    @OneToMany(mappedBy = "user")
    private Collection<Report> reports;

    @Schema(description = "Список испытательных периодов (на случай если пользователь заберет несколько животных")
    @JsonIgnore
    @OneToMany
    private Collection<TrialPeriod> trialPeriods;

    @Schema(description = "Стэк переходов пользователя по меню")
    @OneToMany(mappedBy = "user")
    private Set<MenuStack> menuStackSet;

    @Schema(description = "у пользователя ID волонтера, у волонтера ID пользователя с который идет диалог")
    private Long companion;

    public User() {
    }

    public User(Long chatId, String name) {
        this.chatId = chatId;
        this.name = name;
    }

    public Collection<TrialPeriod> getTrialPeriods() {
        return trialPeriods;
    }

    public void setTrialPeriods(Collection<TrialPeriod> trialPeriods) {
        this.trialPeriods = trialPeriods;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setCompanion(Long companion) {
        this.companion = companion;
    }

    public Long getCompanion() {
        return this.companion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId()) && getChatId().equals(user.getChatId()) && getName().equals(user.getName()) && getRole() == user.getRole();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getChatId(), getName(), getRole());
    }

    @Override
    public String toString() {
        return "User{" +
                "Id=" + id +
                ", chatId=" + chatId +
                ", name='" + name + '\'' +
                ", role=" + role +
                '}';
    }
}
