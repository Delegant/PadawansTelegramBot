package pro.sky.telegrambot.constants;

import java.util.ResourceBundle;

public class ResponsesText {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");
    public static final String VOLUNTEER_START_TEXT = bundle.getString("VOLUNTEER_START_TEXT");
    public static final String START_TEXT = bundle.getString("START_TEXT");
    public static final String DEFAULT_MENU_TEXT = bundle.getString("DEFAULT_MENU_TEXT");
    public static final String INFO_TEXT = bundle.getString("INFO_TEXT");
    public static final String ABOUT_US = bundle.getString("ABOUT_US");
    public static final String SHELTER_CONTACTS = bundle.getString("SHELTER_CONTACTS");
    public static final String SAFETY_REGULATIONS = bundle.getString("SAFETY_REGULATIONS");
    public static final String SHARE_CONTACT = bundle.getString("SHARE_CONTACT");

    /**
     * Messages for "Consultation" menu
     */

    public static final String CONSULT_MENU_MESSAGE = bundle.getString("CONSULT_MENU");
    public static final String MEETING_WITH_DOG = bundle.getString("MEETING_WITH_DOG");
    public static final String LIST_OF_DOCUMENTS = bundle.getString("LIST_OF_DOCUMENTS");
    public static final String HOW_TO_CARRY_ANIMAL = bundle.getString("HOW_TO_CARRY_ANIMAL");
    public static final String MAKING_HOUSE_FOR_PUPPY = bundle.getString("MAKING_HOUSE_FOR_PUPPY");
    public static final String MAKING_HOUSE_FOR_DOG = bundle.getString("MAKING_HOUSE_FOR_DOG");
    public static final String MAKING_HOUSE_FOR_DOG_WITH_DISABILITIES = bundle.getString("MAKING_HOUSE_FOR_DOG_WITH_DISABILITIES");
    public static final String DOG_HANDLER_ADVICES = bundle.getString("DOG_HANDLER_ADVICES");
    public static final String DOG_HANDLERS = bundle.getString("DOG_HANDLERS");
    public static final String DENY_LIST = bundle.getString("DENY_LIST");
    public static final String ERROR_COMMAND_TEXT = bundle.getString("ERROR_COMMAND_TEXT");

    /**
     * Messages for volunteers menu
     */

    public static final String ADD_PARENT = bundle.getString("ADD_PARENT");
    public static final String CHECK_REPORTS = bundle.getString("CHECK_REPORTS");
    public static final String VIEW_INCOMING_MESSAGES = bundle.getString("VIEW_INCOMING_MESSAGES");
    public static final String TRIAL_PERIOD_FOR_VOLUNTEERS_MENU = bundle.getString("TRIAL_PERIOD_FOR_VOLUNTEERS_MENU");
    public static final String CONTACT_PARENT = bundle.getString("CONTACT_PARENT");

    public static final String APPLY_TRIAL_PERIOD = bundle.getString("APPLY_TRIAL_PERIOD");
    public static final String PROLONG_TRIAL_PERIOD = bundle.getString("PROLONG_TRIAL_PERIOD");
    public static final String DECLINE_TRIAL_PERIOD = bundle.getString("DECLINE_TRIAL_PERIOD");

    public static final String FIND_REPORT_BY_NAME = bundle.getString("FIND_REPORT_BY_NAME");
    public static final String FIND_REPORT_BY_USERID = bundle.getString("FIND_REPORT_BY_USERID");
    public static final String GET_REPORT_BY_ID = bundle.getString("GET_REPORT_BY_ID");
    public static final String UNREAD_REPORTS = bundle.getString("UNREAD_REPORTS");
}
