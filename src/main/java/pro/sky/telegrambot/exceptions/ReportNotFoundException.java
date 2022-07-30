package pro.sky.telegrambot.exceptions;

import java.util.NoSuchElementException;

public class ReportNotFoundException extends NoSuchElementException {

    public ReportNotFoundException() {
    }

    public ReportNotFoundException(String s) {
        super(s);
    }
}
