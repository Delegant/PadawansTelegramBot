package pro.sky.telegrambot.constants;

import java.util.*;

public class ButtonsText {

    private static final Map<String, ButtonsText> buttonsTextMap;
    public static String EXCEPTION;
    public static String HIDDEN;

    static {
        buttonsTextMap = new HashMap<>();
        buttonsTextMap.put("cat", new ButtonsText("cat"));
        buttonsTextMap.put("dog", new ButtonsText(null));
    }

    private final Map<String, List<String>> menuMap = new HashMap<>();
    private final ResourceBundle bundle;

    private ButtonsText(String key) {
        if (key==null) {
            this.bundle = ResourceBundle.getBundle("default");
        } else {
            this.bundle = ResourceBundle.getBundle("default", new Locale(key));
        }
        init();
    }


    public static ButtonsText getButtonText(String key) {
        return buttonsTextMap.get(key);
    }

    public String getString(String key) {
        return bundle.getString(key);
    }

    public List<String> getMenu(String key) {
        return menuMap.get(key);
    }

    private void init() {
        menuMap.put("BASE_MENU", List.of(
                bundle.getString("CAT_BUTTON"),
                bundle.getString("DOG_BUTTON")));
        menuMap.put("BACK_TO_MAIN_MENU", List.of(
                bundle.getString("BACK_TO_MAIN_MENU_BUTTON")));
        menuMap.put("BACK_TO_VOLUNTEERS_MENU", List.of(
                bundle.getString("BACK_TO_VOLUNTEERS_MENU")
        ));
        menuMap.put("MAIN_MENU", List.of(
                bundle.getString("INFO_BUTTON"),
                bundle.getString("HOW_TO_GET_DOG_BUTTON"),
                bundle.getString("SEND_REPORT_BUTTON"),
                bundle.getString("CALL_VOLUNTEER_BUTTON")
        ));
        menuMap.put("INFO_MENU", List.of(
                bundle.getString("ABOUT_US_BUTTON"),
                bundle.getString("CONTACTS_BUTTON"),
                bundle.getString("SAFETY_REGULATIONS_BUTTON"),
                bundle.getString("SHARE_CONTACT_BUTTON"),
                bundle.getString("CALL_VOLUNTEER_BUTTON"),
                bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
        ));
        menuMap.put("HOW_TO_GET_DOG_MENU", List.of(
                bundle.getString("MEETING_WITH_DOG_BUTTON"),
                bundle.getString("LIST_OF_DOCUMENTS_BUTTON"),
                bundle.getString("HOW_TO_CARRY_ANIMAL_BUTTON"),
                bundle.getString("MAKING_HOUSE_BUTTON"),
                bundle.getString("DOG_HANDLER_ADVICES_BUTTON"),
                bundle.getString("DOG_HANDLERS_BUTTON"),
                bundle.getString("DENY_LIST_BUTTON"),
                bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
        ));
        menuMap.put("MAKING_HOUSE_MENU", List.of(
                bundle.getString("FOR_PUPPY_BUTTON"),
                bundle.getString("FOR_DOG_BUTTON"),
                bundle.getString("FOR_DOG_WITH_DISABILITIES_BUTTON"),
                bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
        ));
        menuMap.put("VOLUNTEER_MAIN_MENU", List.of(
                bundle.getString("ADD_PARENT"),
                bundle.getString("CHECK_REPORTS"),
                bundle.getString("VIEW_INCOMING_MESSAGES"),
                bundle.getString("TRIAL_PERIOD_FOR_VOLUNTEERS_MENU"),
                bundle.getString("CONTACT_PARENT")
        ));
        menuMap.put("TRIAL_PERIOD_MENU", List.of(
                bundle.getString("APPLY_TRIAL_PERIOD"),
                bundle.getString("PROLONG_TRIAL_PERIOD"),
                bundle.getString("DECLINE_TRIAL_PERIOD"),
                bundle.getString("BACK_TO_VOLUNTEERS_MENU")
        ));
        menuMap.put("REPORTS_MENU", List.of(
                bundle.getString("FIND_REPORT_BY_NAME"),
                bundle.getString("FIND_REPORT_BY_USERID"),
                bundle.getString("GET_REPORT_BY_ID"),
                bundle.getString("UNREAD_REPORTS"),
                bundle.getString("BACK_TO_VOLUNTEERS_MENU")
        ));
        menuMap.put("ADMIN_MAIN_MENU", List.of(
                bundle.getString("BACK_TO_VOLUNTEERS_MENU")

        ));
        EXCEPTION = bundle.getString("EXCEPTION");
        HIDDEN = bundle.getString("HIDDEN");
    }
}
