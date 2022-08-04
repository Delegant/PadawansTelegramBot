package pro.sky.telegrambot.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.MenuSelector;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class MenuSelectorFactory {
    private final Map<User.Role, MenuSelector> viewerMap = new EnumMap<>(User.Role.class);

    @Autowired
    private MenuSelectorFactory(List<MenuSelector> menuSelectors) {
        for (MenuSelector menuSelector : menuSelectors) {
            viewerMap.put(menuSelector.getRole(), menuSelector);
        }
    }

    public MenuSelector getMenuSelector(User.Role role) {
        MenuSelector menuSelector = viewerMap.get(role);
        if (menuSelector == null) {
            throw new IllegalArgumentException();
        }
        return menuSelector;
    }
}
