package pro.sky.telegrambot.constants;

import java.util.List;
import java.util.ResourceBundle;

public class ButtonsText {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");
    public static final String START = bundle.getString("START");
    public static final String INFO_BUTTON = bundle.getString("INFO_BUTTON");
    public static final String HOW_TO_GET_DOG_BUTTON = bundle.getString("HOW_TO_GET_DOG_BUTTON");
    public static final String SEND_REPORT_BUTTON = bundle.getString("SEND_REPORT_BUTTON");
    public static final String CALL_VOLUNTEER_BUTTON = bundle.getString("CALL_VOLUNTEER_BUTTON");
    public static final String EXCEPTION = bundle.getString("EXCEPTION");

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

    //volunteers menu buttons
    public static final String ADD_PARENT_BUTTON = bundle.getString("ADD_PARENT");
    public static final String CHECK_REPORTS_BUTTON = bundle.getString("CHECK_REPORTS");
    public static final String VIEW_INCOMING_MESSAGES_BUTTON = bundle.getString("VIEW_INCOMING_MESSAGES");
    public static final String TRIAL_PERIOD_FOR_VOLUNTEERS_MENU_BUTTON = bundle.getString("TRIAL_PERIOD_FOR_VOLUNTEERS_MENU");
    public static final String CONTACT_PARENT_BUTTON = bundle.getString("CONTACT_PARENT");

    public static final String APPLY_TRIAL_PERIOD_BUTTON = bundle.getString("APPLY_TRIAL_PERIOD");
    public static final String PROLONG_TRIAL_PERIOD_BUTTON = bundle.getString("PROLONG_TRIAL_PERIOD");
    public static final String DECLINE_TRIAL_PERIOD_BUTTON = bundle.getString("DECLINE_TRIAL_PERIOD");

    public static final String FIND_REPORT_BY_NAME_BUTTON = bundle.getString("FIND_REPORT_BY_NAME");
    public static final String FIND_REPORT_BY_USERID_BUTTON = bundle.getString("FIND_REPORT_BY_USERID");
    public static final String GET_REPORT_BY_ID_BUTTON = bundle.getString("GET_REPORT_BY_ID");
    public static final String UNREAD_REPORTS_BUTTON = bundle.getString("UNREAD_REPORTS");

    public static final String BACK_TO_VOLUNTEERS_MENU_BUTTON = "Вернуться в меню волонтеров";


    public static final List<String> BACK_TO_MAIN_MENU = List.of(
            BACK_TO_MAIN_MENU_BUTTON
    );

    public static final List<String> BACK_TO_VOLUNTEERS_MENU = List.of(
            BACK_TO_VOLUNTEERS_MENU_BUTTON
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

    public static final List<String> VOLUNTEER_MAIN_MENU = List.of(
            ADD_PARENT_BUTTON,
            CHECK_REPORTS_BUTTON,
            VIEW_INCOMING_MESSAGES_BUTTON,
            TRIAL_PERIOD_FOR_VOLUNTEERS_MENU_BUTTON,
            CONTACT_PARENT_BUTTON
    );

    public static final List<String> TRIAL_PERIOD_MENU = List.of(
            APPLY_TRIAL_PERIOD_BUTTON,
            PROLONG_TRIAL_PERIOD_BUTTON,
            DECLINE_TRIAL_PERIOD_BUTTON,
            BACK_TO_VOLUNTEERS_MENU_BUTTON

    );

    public static final List<String> REPORTS_MENU = List.of(
            FIND_REPORT_BY_NAME_BUTTON,
            FIND_REPORT_BY_USERID_BUTTON,
            GET_REPORT_BY_ID_BUTTON,
            UNREAD_REPORTS_BUTTON,
            BACK_TO_VOLUNTEERS_MENU_BUTTON
    );

    public static final List<String> ADMIN_MAIN_MENU = List.of(

    );
}
