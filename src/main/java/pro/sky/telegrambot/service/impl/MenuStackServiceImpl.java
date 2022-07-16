package pro.sky.telegrambot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.repository.MenuStackRepository;
import pro.sky.telegrambot.service.MenuStackService;
@Service
public class MenuStackServiceImpl implements MenuStackService {

    private final MenuStackRepository menuStackRepository;

    public MenuStackServiceImpl(MenuStackRepository menuStackRepository) {
        this.menuStackRepository = menuStackRepository;
    }

    @Override
    public String getTextPackKey(Long chatId) {
        return menuStackRepository.findTextPackageKeyByChatId(chatId).get();
    }

    @Override
    public String getLastMenuState(Long chatId) {
        return menuStackRepository.findMenuStateChatId(chatId).get();
    }
}
