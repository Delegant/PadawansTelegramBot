package pro.sky.telegrambot.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepoServiceTest {
    public static final String NAME = "Luk";
    private static final Long EXPECTED_ID = 1L;
    private static final Long CHAT_ID = 123L;
    public static final User newUser = new User(CHAT_ID, NAME);
    public static final User userFromDataBase = new User(CHAT_ID, NAME);
    public static final User adminFromDataBase = new User(CHAT_ID, NAME);
    public static final User.Role adminRole = User.Role.ADMIN;

    static {
        userFromDataBase.setId(EXPECTED_ID);
        adminFromDataBase.setId(EXPECTED_ID);
        adminFromDataBase.setRole(adminRole);
    }

    @InjectMocks
    UserRepoService out;
    @Mock
    private UserRepository userRepository;

    @Test
    void isUserCreated() {
        when(userRepository.save(eq(newUser))).then(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", EXPECTED_ID);
            return user;
        });
        assertEquals(out.createUser(CHAT_ID, NAME), userFromDataBase);
    }

    @Test
    void isRoleAsMarkedIfUserExists() {
        when(userRepository.findUserByChatId(CHAT_ID)).thenReturn(Optional.of(userFromDataBase));
        when(userRepository.save(adminFromDataBase)).thenReturn(adminFromDataBase);
        assertEquals(out.markRole(CHAT_ID, adminRole), Optional.of(adminFromDataBase));
    }

    @Test
    void isRoleAsMarkedIfUserNotExists() {
        when(userRepository.findUserByChatId(CHAT_ID)).thenReturn(Optional.empty());
        assertEquals(out.markRole(CHAT_ID, adminRole), Optional.empty());
    }

    @Test
    void isGetUserChatIdReturnOptionalWithUser(){
        when(userRepository.findUserByChatId(CHAT_ID)).thenReturn(Optional.of(userFromDataBase));
        assertEquals(out.getUserByChatId(CHAT_ID), Optional.of(userFromDataBase));
    }

    @Test
    void isGetUserChatIdReturnOptionalWithoutUser(){
        when(userRepository.findUserByChatId(CHAT_ID)).thenReturn(Optional.empty());
        assertEquals(out.getUserByChatId(CHAT_ID), Optional.empty());
    }
}