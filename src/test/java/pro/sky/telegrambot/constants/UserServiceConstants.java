package pro.sky.telegrambot.constants;

import pro.sky.telegrambot.model.User;

public class UserServiceConstants {
    public static final String NAME = "Luk";
    public static final Long EXPECTED_ID = 1L;
    public static final Long CHAT_ID = 123L;
    public static final User newUser = new User(CHAT_ID, NAME);
    public static final User userFromDataBase = new User(CHAT_ID, NAME);
    public static final User adminFromDataBase = new User(CHAT_ID, NAME);
    public static final User.Role adminRole = User.Role.ADMIN;

    static {
        userFromDataBase.setId(EXPECTED_ID);
        adminFromDataBase.setId(EXPECTED_ID);
        adminFromDataBase.setRole(adminRole);
    }
}
