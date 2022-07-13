package pro.sky.telegrambot.service.impl;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.exceptions.MessageNotFoundException;
import pro.sky.telegrambot.model.IncomingMessage;
import pro.sky.telegrambot.repository.IncomingMessageRepository;
import pro.sky.telegrambot.service.MessageService;

import java.util.Collection;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private IncomingMessageRepository messageRepository;

    public MessageServiceImpl(IncomingMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Collection<IncomingMessage> getUnreadIncomingMessages() {
        return messageRepository.findAllByReadStatusEquals(String.valueOf(IncomingMessage.ReadStatus.UNREAD));
    }

    @Override
    public IncomingMessage getIncomingMessage(Long messageId) {
        return messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("!!!! Message with id " +  messageId + " not found"));
    }
}
