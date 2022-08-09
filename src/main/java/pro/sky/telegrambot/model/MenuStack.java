package pro.sky.telegrambot.model;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "menustack")
public class MenuStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Schema(description = "текстовый пакет пользователя")
    @Column(columnDefinition = "varchar(255) default 'DOG'")
    private String textPackKey;
    @Schema(description = "отправленный текст")
    private String textKey;
    @Schema(description = "отправленное меню пользователя")
    @Column(nullable = false, columnDefinition = "varchar(255) default 'SPECIES_PET_SELECTION_MENU'")
    private String menuState;
    @Schema(description = "Тип данных которые мы ожидаем получить от пользователя")
    @Column(name = "expect")
    @Enumerated(EnumType.STRING)
    private MessageType expect;

    public MenuStack() {
    }

    public MenuStack(User user) {
        this.menuState = "SPECIES_PET_SELECTION_MENU";
        this.textPackKey = "-2057967076";
        this.textKey = "DEFAULT_MENU_TEXT";
        this.user = user;
        this.expect = MessageType.COMMAND;
    }

    public MenuStack(User user, String textKey, String menuState) {
        this(user);
        this.textKey = textKey;
        this.menuState = menuState;
    }

    public MenuStack(User user, String textPackKey, String textKey, String menuState) {
        this(user, textKey, menuState);
        this.textPackKey = textPackKey;
    }

    public MessageType getExpect() {
        return expect;
    }

    public void setExpect(MessageType expect) {
        this.expect = expect;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTextPackKey() {
        return textPackKey;
    }

    public void setTextPackKey(String textPackKey) {
        this.textPackKey = textPackKey;
    }

    public String getTextKey() {
        return textKey;
    }

    public void setTextKey(String textKey) {
        this.textKey = textKey;
    }

    public String getMenuState() {
        return menuState;
    }

    public void setMenuState(String menuState) {
        this.menuState = menuState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        MenuStack menuStack = (MenuStack) o;
        return Objects.equals(menuStack.getId(), this.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
    /**
     * Enum для отражения ожидаемого типа следующего update
     */
    public enum MessageType {
        COMMAND,
        COMMAND_TEXT,
        COMMAND_CALL_BACK,
        REPORT_TEXT,
        REPORT_PIC,
        DIALOG_COMMAND,
        DIALOG_TEXT,
        DIALOG_PIC,
        DIALOG,
        REPORT,
        REPORT_REQUEST,
        REPORT_LIST,
        READING_REPORT,
        REPORT_ACTION,
        COMMAND_OR_TEXT,
        DIALOG_REQUEST,
        TELEGRAM_USER_ID,
        USER_NAME,
        ADDING_VOLUNTEER,
        ADDING_PARENT,
        ADDING_ADMIN,
        RECEIVED_REPORT_NOTIFICATION,
        UPDATING_REPORT,
        MY_REPORTS,
        TRIAL_PERIOD,
        TRIAL_PERIOD_LIST,
        TRIAL_PERIOD_ACTION
    }
}
