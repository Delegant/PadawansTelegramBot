package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.MenuService;

import javax.annotation.PostConstruct;
import java.util.List;

import static pro.sky.telegrambot.constants.ButtonsText.MAIN_MENU;
import static pro.sky.telegrambot.constants.ResponsesText.START_TEXT;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);


    private TelegramBot telegramBot;

    private final MenuService menuService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, MenuService menuService) {
        this.telegramBot = telegramBot;
        this.menuService = menuService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            Message message = update.message();
            if (message.text().equals("/start")) {
                telegramBot.execute(menuService.menuLoader(message, START_TEXT, MAIN_MENU));
            }



        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    // метод handleCallBack()
    //switch(callBack)
    // case: menuLoader()
}
