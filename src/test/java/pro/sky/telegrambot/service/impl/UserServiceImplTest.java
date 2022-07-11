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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static pro.sky.telegrambot.constants.UserServiceConstants.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl out;
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