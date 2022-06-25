package pro.sky.telegrambot.constants;

import java.util.ResourceBundle;

public class ResponsesText {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");
    public static final String START_TEXT = bundle.getString("START_TEXT");
    public static final String INFO_TEXT = bundle.getString("INFO_TEXT");
    public static final String ABOUT_US = bundle.getString("ABOUT_US");
    public static final String SHELTER_CONTACTS = bundle.getString("SHELTER_CONTACTS");
    public static final String SAFETY_REGULATIONS = bundle.getString("SAFETY_REGULATIONS");
    public static final String SHARE_CONTACT = bundle.getString("SHARE_CONTACT");
    /** Messages for "Consultaion" menu*/

    public static final String CONSULT_MENU_MESSAGE = bundle.getString("CONSULT_MENU");
    public static final String MEETING_RULES_MESSAGE = bundle.getString("MEETING_RULES");
    public static final String DOC_LIST_MESSAGE = bundle.getString("DOC_LIST");
    public static final String TRANSPORT_RECOMMENDATION_MESSAGE = bundle.getString("TRANSPORT_RECOMMENDATION");
    public static final String HOUSE_PUPPY_MESSAGE = bundle.getString("HOUSE_PUPPY");
    public static final String HOUSE_OLD_MESSAGE = bundle.getString("HOUSE_OLD");
    public static final String HOUSE_DIS_MESSAGE = bundle.getString("HOUSE_DIS");
    public static final String SPEC_RECOMMENDATION_MESSAGE = bundle.getString("SPEC_RECOMMENDATION");
    public static final String SPEC_FEEDBACK_MESSAGE = bundle.getString("SPEC_FEEDBACK");
    public static final String REJECT_PET_MESSAGE = bundle.getString("REJECT_PET");

}


