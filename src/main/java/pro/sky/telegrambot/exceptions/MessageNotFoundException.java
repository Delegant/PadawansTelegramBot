package pro.sky.telegrambot.exceptions;

import java.util.NoSuchElementException;

public class MessageNotFoundException extends NoSuchElementException {

    public MessageNotFoundException() {
    }

    public MessageNotFoundException(String s) {
        super(s);
    }
}
