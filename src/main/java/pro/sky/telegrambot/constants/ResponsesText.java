package pro.sky.telegrambot.constants;

import java.util.ResourceBundle;

public class ResponsesText {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("default");
    public static final String START_TEXT = bundle.getString("START_TEXT");
    public static final String DEFAULT_MENU_TEXT = bundle.getString("DEFAULT_MENU_TEXT");
    public static final String INFO_TEXT = bundle.getString("INFO_TEXT");
    public static final String ABOUT_US = bundle.getString("ABOUT_US");
    public static final String SHELTER_CONTACTS = bundle.getString("SHELTER_CONTACTS");
    public static final String SAFETY_REGULATIONS = bundle.getString("SAFETY_REGULATIONS");
    public static final String SHARE_CONTACT = bundle.getString("SHARE_CONTACT");
    public static final String RECOMMENDATIONS = bundle.getString("RECOMMENDATIONS");

}
