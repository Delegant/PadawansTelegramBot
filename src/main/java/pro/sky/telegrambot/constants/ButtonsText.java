package pro.sky.telegrambot.constants;

import java.util.List;
import java.util.ResourceBundle;

public class ButtonsText {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");
    public static final String INFO_BUTTON = bundle.getString("INFO_BUTTON");
    public static final String HOW_TO_GET_DOG_BUTTON = bundle.getString("HOW_TO_GET_DOG_BUTTON");
    public static final String SEND_REPORT_BUTTON = bundle.getString("SEND_REPORT_BUTTON");
    public static final String CALL_VOLUNTEER_BUTTON = bundle.getString("CALL_VOLUNTEER_BUTTON");

    public static final String ABOUT_US_BUTTON = bundle.getString("ABOUT_US_BUTTON");
    public static final String CONTACTS_BUTTON = bundle.getString("CONTACTS_BUTTON");
    public static final String SAFETY_REGULATIONS_BUTTON = bundle.getString("SAFETY_REGULATIONS_BUTTON");
    public static final String SHARE_CONTACT_BUTTON = bundle.getString("SHARE_CONTACT_BUTTON");
    public static final String BACK_TO_MAIN_MENU_BUTTON = bundle.getString("BACK_TO_MAIN_MENU_BUTTON");

    public static final String MEETING_WITH_DOG_BUTTON = bundle.getString("MEETING_WITH_DOG_BUTTON");
    public static final String LIST_OF_DOCUMENTS_BUTTON = bundle.getString("LIST_OF_DOCUMENTS_BUTTON");
    public static final String HOW_TO_CARRY_ANIMAL_BUTTON = bundle.getString("HOW_TO_CARRY_ANIMAL_BUTTON");
    public static final String MAKING_HOUSE_BUTTON = bundle.getString("MAKING_HOUSE_BUTTON");
    public static final String DOG_HANDLER_ADVICES_BUTTON = bundle.getString("DOG_HANDLER_ADVICES_BUTTON");
    public static final String DOG_HANDLERS_BUTTON = bundle.getString("DOG_HANDLERS_BUTTON");
    public static final String DENY_LIST_BUTTON = bundle.getString("DENY_LIST_BUTTON");

    public static final String FOR_PUPPY_BUTTON = bundle.getString("FOR_PUPPY_BUTTON");
    public static final String FOR_DOG_BUTTON = bundle.getString("FOR_DOG_BUTTON");
    public static final String FOR_DOG_WITH_DISABILITIES_BUTTON = bundle.getString("FOR_DOG_WITH_DISABILITIES_BUTTON");


    public static final List<String> BACK_TO_MAIN_MENU = List.of(
            BACK_TO_MAIN_MENU_BUTTON
    );

   public static final List<String> MAIN_MENU = List.of(
           INFO_BUTTON,
           HOW_TO_GET_DOG_BUTTON,
           SEND_REPORT_BUTTON,
           CALL_VOLUNTEER_BUTTON
   );

   public static final List<String> INFO_MENU = List.of(
           ABOUT_US_BUTTON,
           CONTACTS_BUTTON,
           SAFETY_REGULATIONS_BUTTON,
           SHARE_CONTACT_BUTTON,
           CALL_VOLUNTEER_BUTTON,
           BACK_TO_MAIN_MENU_BUTTON
   );

   public static final List<String> HOW_TO_GET_DOG_MENU = List.of(
            MEETING_WITH_DOG_BUTTON,
            LIST_OF_DOCUMENTS_BUTTON,
            HOW_TO_CARRY_ANIMAL_BUTTON,
            MAKING_HOUSE_BUTTON,
            DOG_HANDLER_ADVICES_BUTTON,
            DOG_HANDLERS_BUTTON,
            DENY_LIST_BUTTON,
            BACK_TO_MAIN_MENU_BUTTON
   );

    public static final List<String> MAKING_HOUSE_MENU = List.of(
            FOR_PUPPY_BUTTON,
            FOR_DOG_BUTTON,
            FOR_DOG_WITH_DISABILITIES_BUTTON,
            BACK_TO_MAIN_MENU_BUTTON
    );
}
