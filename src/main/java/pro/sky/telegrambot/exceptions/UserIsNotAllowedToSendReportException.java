package pro.sky.telegrambot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.NoSuchElementException;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UserIsNotAllowedToSendReportException extends NoSuchElementException {

    public UserIsNotAllowedToSendReportException(String message) {
        super(message);
    }

    public UserIsNotAllowedToSendReportException() {

    }

}
