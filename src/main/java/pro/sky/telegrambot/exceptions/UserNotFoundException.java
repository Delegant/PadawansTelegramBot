package pro.sky.telegrambot.exceptions;

import java.util.NoSuchElementException;

public class UserNotFoundException extends NoSuchElementException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException() {
    }
}
