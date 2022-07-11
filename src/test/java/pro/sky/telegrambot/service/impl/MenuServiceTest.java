package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.sky.telegrambot.constants.MenuServiceConstants.*;

public class MenuServiceTest {

    private MenuServiceImpl menuService;
    private Message message;
    private Update update;

    @BeforeEach
    private void setUp() {
        menuService = new MenuServiceImpl();
    }

    @Test //keyboardFactory
    public void menuLoader() {
        assertThrows(RuntimeException.class, () -> menuService.menuLoader(message, TEXT_MESSAGE, TEST_MENU_4));
        assertThrows(RuntimeException.class, () -> menuService.menuLoader(update, TEXT_MESSAGE, TEST_MENU_4));
    }




}
