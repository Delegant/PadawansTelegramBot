package pro.sky.telegrambot.constants;

import java.util.*;

public class ButtonsText {

    private static final String hashPetDefaultTextKey = "-2057967076";
    private static String SEND_REPORT_IS_CREATED;
    private static String ASK_TO_SEND_PIC;
    private static String ASK_TO_SEND_TEXT;
    public static String HIDDEN_BUTTON;
    private static ButtonsText singletonBundleText;
    private final Map<String, ResourceBundle> bundleMap;
    private final Map<String, List<String>> menuMap = new HashMap<>();
    private ResourceBundle bundle;
    private String currentTextKey;

    {
        bundleMap = new HashMap<>();
        bundleMap.put("-55733391", ResourceBundle.getBundle("default", new Locale("cat")));
        bundleMap.put("-2057967076", ResourceBundle.getBundle("default"));
    }

    private ButtonsText() {
        currentTextKey = hashPetDefaultTextKey;
        bundle = bundleMap.get(currentTextKey);
        init();
    }

    public static ButtonsText getButtonText(String textPackKey) {
        if (singletonBundleText == null) {
            singletonBundleText = new ButtonsText();
        }
        singletonBundleText.changeCurrentTextKey(textPackKey);
        return singletonBundleText;
    }

    public void changeCurrentTextKey(String newCurrentTextKey) {
        this.currentTextKey = newCurrentTextKey;
        bundle = bundleMap.get(currentTextKey);
        init();
    }

    public String getString(String key) {
        return bundle.getString(key);
    }

    public List<String> getMenu(String key) {
        return menuMap.get(bundle.getString(key));
    }

    private void init() {
        menuMap.put(bundle.getString("SPECIES_PET_SELECTION_MENU"),
                List.of(
                        bundle.getString("CAT_BUTTON"),
                        bundle.getString("DOG_BUTTON")));
        menuMap.put(bundle.getString("BACK_TO_MAIN_MENU"),
                List.of(
                        bundle.getString("BACK_BUTTON"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")));
        menuMap.put("BACK_TO_VOLUNTEERS_MENU",
                List.of(
                        bundle.getString("BACK_BUTTON"),
                        bundle.getString("BACK_TO_VOLUNTEERS_MENU")
                ));
        menuMap.put(bundle.getString("MAIN_MENU"),
                List.of(
                        bundle.getString("INFO_BUTTON"),
                        bundle.getString("HOW_TO_GET_PET_BUTTON"),
                        bundle.getString("SEND_REPORT_BUTTON"),
                        bundle.getString("MY_REPORTS"),
                        bundle.getString("MY_TRIAL_PERIOD"),
                        bundle.getString("CALL_VOLUNTEER_BUTTON"),
                        bundle.getString("CHANGE_PET_BUTTON")
                ));
        menuMap.put(bundle.getString("INFO_MENU"),
                List.of(
                        bundle.getString("ABOUT_US_BUTTON"),
                        bundle.getString("CONTACTS_BUTTON"),
                        bundle.getString("SAFETY_REGULATIONS_BUTTON"),
                        bundle.getString("SHARE_CONTACT_BUTTON"),
                        bundle.getString("CALL_VOLUNTEER_BUTTON"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("HOW_TO_GET_PET_MENU"),
                List.of(
                        bundle.getString("MEETING_WITH_PET_BUTTON"),
                        bundle.getString("LIST_OF_DOCUMENTS_BUTTON"),
                        bundle.getString("HOW_TO_CARRY_ANIMAL_BUTTON"),
                        bundle.getString("MAKING_HOUSE_BUTTON"),
                        bundle.getString("DOG_HANDLER_ADVICES_BUTTON"),
                        bundle.getString("DOG_HANDLERS_BUTTON"),
                        bundle.getString("DENY_LIST_BUTTON"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("MAKING_HOUSE_MENU"),
                List.of(
                        bundle.getString("FOR_PUPPY_BUTTON"),
                        bundle.getString("FOR_PET_BUTTON"),
                        bundle.getString("FOR_PET_WITH_DISABILITIES_BUTTON"),
                        bundle.getString("BACK_BUTTON"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("IN_REPORT_SEND_TEXT_MENU"),
                List.of(
                        bundle.getString("FINISH_SENDING_REPORT"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("IN_REPORT_SEND_PIC_MENU"),
                List.of(
                        bundle.getString("FINISH_SENDING_REPORT"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("VOLUNTEER_MAIN_MENU"),
                List.of(
                        bundle.getString("ADD_PARENT"),
                        bundle.getString("CHECK_REPORTS"),
                        bundle.getString("VIEW_INCOMING_MESSAGES"),
                        bundle.getString("TRIAL_PERIOD_FOR_VOLUNTEERS_MENU"),
                        bundle.getString("CONTACT_PARENT")
                ));
        menuMap.put(bundle.getString("BACK_TO_VOLUNTEERS_MENU"),
                List.of(
                        bundle.getString("VOLUNTEER_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("TRIAL_PERIOD_MENU"),
                List.of(
                        bundle.getString("APPLY_TRIAL_PERIOD"),
                        bundle.getString("PROLONG_TRIAL_PERIOD"),
                        bundle.getString("DECLINE_TRIAL_PERIOD"),
                        bundle.getString("BACK_TO_VOLUNTEERS_MENU")
                ));
        menuMap.put(bundle.getString("REPORTS_MENU"),
                List.of(
                        bundle.getString("FIND_REPORT_BY_NAME"),
                        bundle.getString("FIND_REPORT_BY_USERID"),
                        bundle.getString("GET_REPORT_BY_ID"),
                        bundle.getString("UNREAD_REPORTS"),
                        bundle.getString("BACK_TO_VOLUNTEERS_MENU")
                ));
        menuMap.put(bundle.getString("INSIDE_REPORT_MENU"),
                List.of(
                        bundle.getString("ASK_TO_SEND_PHOTO"),
                        bundle.getString("ASK_TO_UPDATE_TEXT"),
                        bundle.getString("SET_AS_READ"),
                        bundle.getString("BACK_TO_REPORT_LIST"),
                        bundle.getString("VOLUNTEER_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("MY_REPORTS_MENU"),
                List.of(
                        bundle.getString("ALL_REPORTS"),
                        bundle.getString("UPDATE_REQUESTED")
                )
        );
        menuMap.put(bundle.getString("INSIDE_PARENT_REPORT_MENU"),
                List.of(
                        bundle.getString("UPDATE_TEXT_FOR_REPORT"),
                        bundle.getString("UPDATE_PICTURE_FOR_REPORT"),
                        bundle.getString("DO_IT_LATER"),
                        bundle.getString("MY_REPORTS"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("ADMIN_MAIN_MENU"),
                List.of(
                        bundle.getString("ADD_PARENT"),
                        bundle.getString("CHECK_REPORTS"),
                        bundle.getString("VIEW_INCOMING_MESSAGES"),
                        bundle.getString("TRIAL_PERIOD_FOR_ADMIN_MENU"),
                        bundle.getString("CONTACT_PARENT"),
                        bundle.getString("ADD_VOLUNTEER"),
                        bundle.getString("ADD_ADMIN")

                ));
        menuMap.put(bundle.getString("REPORTS_MENU_ADMIN"),
                List.of(
                        bundle.getString("FIND_REPORT_BY_NAME"),
                        bundle.getString("FIND_REPORT_BY_USERID"),
                        bundle.getString("GET_REPORT_BY_ID"),
                        bundle.getString("UNREAD_REPORTS"),
                        bundle.getString("BACK_TO_ADMIN_MENU")
                ));
        menuMap.put(bundle.getString("TRIAL_PERIOD_MENU_ADMIN"),
                List.of(
                        bundle.getString("APPLY_TRIAL_PERIOD"),
                        bundle.getString("PROLONG_TRIAL_PERIOD"),
                        bundle.getString("DECLINE_TRIAL_PERIOD"),
                        bundle.getString("BACK_TO_ADMIN_MENU")
                ));
        menuMap.put(bundle.getString("CALL_VOLUNTEER_MENU"),
                List.of(bundle.getString("CALL_VOLUNTEER_BUTTON"),
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));
        menuMap.put(bundle.getString("TO_SUPPORT_ACCEPT_MENU"),
                List.of(bundle.getString("ACCEPT_DIALOG")
                ));
        menuMap.put(bundle.getString("TO_SUPPORT_DENY_MENU"),
                List.of(bundle.getString("DENY_DIALOG")
                ));
        menuMap.put(bundle.getString("BACK_TO_ONLY_MAIN_MENU"),
                List.of(
                        bundle.getString("BACK_TO_MAIN_MENU_BUTTON")
                ));

        ASK_TO_SEND_TEXT = bundle.getString("ASK_TO_SEND_TEXT");
        ASK_TO_SEND_PIC = bundle.getString("ASK_TO_SEND_PIC");
        SEND_REPORT_IS_CREATED = bundle.getString("SEND_REPORT_IS_CREATED");

        HIDDEN_BUTTON = bundle.getString("HIDDEN_BUTTON");
    }
}
