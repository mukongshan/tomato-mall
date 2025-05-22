package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Message;
import com.example.tomatomall.repository.MessageRepository;
import com.example.tomatomall.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    @Transactional
    public String sendMessage(Message message) {
        messageRepository.save(message);
        return "消息成功发送";
    }

    @Override
    public List<Message> getMessagesByUserId(Integer userId) {
        return messageRepository.findByToUserOrderByCreatedTimeDesc(userId);
    }

    @Override
    @Transactional
    public void markMessageAsRead(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        message.setIsRead(true);
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public void deleteMessage(Integer messageId) {
        messageRepository.deleteById(messageId);
    }

    @Override
    public long getUnreadMessageCount(Integer userId) {
        return messageRepository.countByToUserAndIsReadFalse(userId);
    }
}