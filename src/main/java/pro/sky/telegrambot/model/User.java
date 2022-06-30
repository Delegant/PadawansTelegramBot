package pro.sky.telegrambot.model;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Schema(description = "Пользователь")
@Entity(name = "users")
public class User {

    public enum Role {
        USER,
        PARENT,
        VOLUNTEER,
        ADMIN,
    }

    @Schema(description = "Идентификатор пользователя")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Schema(description = "Идентификатор пользователя в Телеграм")
    private Long chatId;

    @Schema(description = "Имя пользователя")
    private String name;

    @Schema(description = "'Роль' пользователя")
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Schema(description = "Отчеты пользователя")
    @OneToMany(mappedBy = "user")
    private Collection<Report> reports;

    public User() {
    }

    public User(Long chatId, String name) {
        this.chatId = chatId;
        this.name = name;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getId().equals(user.getId()) && getChatId().equals(user.getChatId()) && getName().equals(user.getName()) && getRole() == user.getRole();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getChatId(), getName(), getRole());
    }

    @Override
    public String toString() {
        return "User{" +
                "Id=" + Id +
                ", chatId=" + chatId +
                ", name='" + name + '\'' +
                ", role=" + role +
                '}';
    }
}
