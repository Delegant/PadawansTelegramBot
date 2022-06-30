package pro.sky.telegrambot.constants;

import java.util.List;
import java.util.ResourceBundle;

public class ButtonsText {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");
    public static final String INFO = bundle.getString("INFO_BUTTON");
    public static final String ABOUT_US = bundle.getString("ABOUT_US_BUTTON");
    public static final String CONTACTS = bundle.getString("CONTACTS_BUTTON");
    public static final String SAFETY_REGULATIONS = bundle.getString("SAFETY_REGULATIONS_BUTTON");
    public static final String SHARE_CONTACT = bundle.getString("SHARE_CONTACT_BUTTON");
    public static final String HOW_TO_GET_DOG = bundle.getString("HOW_GET_DOG_BUTTON");
    public static final String SEND_REPORT = bundle.getString("SENT_REPORT_BUTTON");
    public static final String BACK_TO_MAIN_MENU_BUTTON = bundle.getString("BACK_TO_MAIN_MENU_BUTTON");

    public static final String CALL_VOLUNTEER = bundle.getString("CALL_VOLUNTEER_BUTTON");

    public static final String RECOMMENDATIONS_BUTTON = bundle.getString("RECOMMENDATIONS_BUTTON");

    public static final String RECOMMENDATIONS = "Рекомендации";

    public static final String MEETING_WITH_DOG_BUTTON = bundle.getString("MEETING_WITH_DOG");
    public static final String LIST_OF_DOCUMENTS_BUTTON = bundle.getString("LIST_OF_DOCUMENTS");
    public static final String HOW_TO_CARRY_ANIMAL_BUTTON = bundle.getString("HOW_TO_CARRY_ANIMAL");
    public static final String MAKING_HOUSE_FOR_PUPPY_BUTTON = bundle.getString("MAKING_HOUSE_FOR_PUPPY");
    public static final String MAKING_HOUSE_FOR_DOG_BUTTON = bundle.getString("MAKING_HOUSE_FOR_DOG");
    public static final String MAKING_HOUSE_FOR_DOG_WITH_DISABILITIES_BUTTON = bundle.getString("MAKING_HOUSE_FOR_DOG_WITH_DISABILITIES");
    public static final String DOG_HANDLER_ADVICES_BUTTON = bundle.getString("DOG_HANDLER_ADVICES");
    public static final String DOG_HANDLERS_BUTTON = bundle.getString("DOG_HANDLERS");
    public static final String DENY_LIST_BUTTON = bundle.getString("DENY_LIST");

    public static final String PROCEDURE_MENU_BUTTON = bundle.getString("PROCEDURE_MENU_BUTTON");
    public static final String DOG_HANDLERS_MENU_BUTTON = bundle.getString("DOG_HANDLERS_MENU_BUTTON");
    public static final String MAKING_HOUSE_MENU_BUTTON = bundle.getString("MAKING_HOUSE_MENU_BUTTON");

    public static final List<String> BACK_TO_MAIN_MENU = List.of(
            BACK_TO_MAIN_MENU_BUTTON
    );
    public static final List<String> MAIN_MENU = List.of(
            INFO,
            RECOMMENDATIONS,
            ABOUT_US,
            CONTACTS,
            CALL_VOLUNTEER);
    public static final List<String> INFO_MENU = List.of(
            ABOUT_US,
            CONTACTS,
            SAFETY_REGULATIONS,
            SHARE_CONTACT,
            CALL_VOLUNTEER,
            BACK_TO_MAIN_MENU_BUTTON );

    public static final List<String> RECOMMENDATIONS_MENU = List.of(
            HOW_TO_CARRY_ANIMAL_BUTTON,
            PROCEDURE_MENU_BUTTON,
            DOG_HANDLERS_MENU_BUTTON,
            MAKING_HOUSE_MENU_BUTTON,
            BACK_TO_MAIN_MENU_BUTTON );

    public static final List<String> PROCEDURE_MENU = List.of(
            MEETING_WITH_DOG_BUTTON,
            LIST_OF_DOCUMENTS_BUTTON,
            DENY_LIST_BUTTON);

    public static final List<String> DOG_HANDLERS_MENU = List.of(
            DOG_HANDLER_ADVICES_BUTTON,
            DOG_HANDLERS_BUTTON
    );

    public static final List<String> MAKING_HOUSE_MENU = List.of(
            MAKING_HOUSE_FOR_DOG_BUTTON,
            MAKING_HOUSE_FOR_PUPPY_BUTTON,
            MAKING_HOUSE_FOR_DOG_WITH_DISABILITIES_BUTTON
    );
}
