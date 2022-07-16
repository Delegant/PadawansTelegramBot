package pro.sky.telegrambot.model;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class MenuStack {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "chat_id")
    private User user;

    @Schema(description = "текущий текстовый пакет пользователя")
    private String textPackageKey;

    @Schema(description = "текущее меню пользователя")
    private String menuState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()){
            return false;
        }
        MenuStack menuStack = (MenuStack) o;
        return Objects.equals(menuStack.getId(), this.getId());
    }

    @Override
    public int hashCode(){
        return Objects.hash(getId());
    }
}
