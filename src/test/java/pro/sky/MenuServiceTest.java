package pro.sky;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.checkerframework.checker.guieffect.qual.UIPackage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.sky.telegrambot.service.impl.MenuServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MenuServiceTest {

    public static final String TEST_1 = "Test_1";
    public static final String TEST_2 = "Test_2";
    public static final String TEST_3 = "Test_3";
    public static final String TEST_4 = "Test_4";
    public static final String TEST_5 = "Test_5";
    public static final String TEST_6 = "Test_6";
    public static final String TEST_7 = "Test_7";

    public static final List<String> TEST_MENU_4 = List.of(TEST_1, TEST_2, TEST_3, TEST_4);
    public static final List<String> TEST_MENU_6 = List.of(TEST_1, TEST_2, TEST_3, TEST_4,TEST_5,TEST_6);
    public static final List<String> TEST_MENU_7 = List.of(TEST_1, TEST_2, TEST_3, TEST_4,TEST_5,TEST_6,TEST_7);


    private MenuServiceImpl menuService;
    private Message message;
    private Update update;
    public static final String TEXT_MESSAGE = "Test_text_message";

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
