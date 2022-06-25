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
    public static final String HOW_GET_DOG = bundle.getString("HOW_GET_DOG_BUTTON");
    public static final String SENT_REPORT = bundle.getString("SENT_REPORT_BUTTON");
    public static final String CALL_VOLUNTEER = bundle.getString("CALL_VOLUNTEER_BUTTON");
    public static final List<String> MAIN_MENU = List.of(INFO, ABOUT_US, CONTACTS, CALL_VOLUNTEER);



    /** The list of buttons for menu "Consultation"*/

    public static final String MEETING_RULES = bundle.getString("MEETING_RULES_BUTTON");
    public static final String DOC_LIST = bundle.getString("DOC_LIST_BUTTON");
    public static final String TRANSPORT_RECOMMENDATION = bundle.getString("TRANSPORT_RECOMMENDATION_BUTTON");
    public static final String HOUSE_PUPPY = bundle.getString("HOUSE_PUPPY_BUTTON");
    public static final String HOUSE_OLD = bundle.getString("HOUSE_OLD_BUTTON");
    public static final String HOUSE_DIS = bundle.getString("HOUSE_DIS_BUTTON");
    public static final String SPEC_RECOMMENDATION = bundle.getString("SPEC_RECOMMENDATION_BUTTON");
    public static final String SPEC_FEEDBACK = bundle.getString("SPEC_FEEDBACK_BUTTON");
    public static final String REJECT_PET = bundle.getString("REJECT_PET_BUTTON");
    public static final List<String> CONSULT_MENU = List.of(MEETING_RULES, DOC_LIST, TRANSPORT_RECOMMENDATION, HOUSE_PUPPY,HOUSE_OLD, HOUSE_DIS, SPEC_RECOMMENDATION,SPEC_FEEDBACK, REJECT_PET);


    /** The text data is executed by consult menu buttons  */



}
