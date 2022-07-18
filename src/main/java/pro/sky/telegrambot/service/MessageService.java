package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.IncomingMessage;

import java.util.Collection;
import java.util.List;

public interface MessageService {

    Collection<IncomingMessage> getUnreadIncomingMessages();

    IncomingMessage getIncomingMessage(Long messageId);
}
