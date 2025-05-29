package com.example.tomatomall.service;

import com.example.tomatomall.po.Message;
import java.util.List;

public interface MessageService {
    String sendMessage(Message message);
    List<Message> getMessagesByToUserId(Integer userId);
    List<Message> getMessagesByFromUserId(Integer userId);
    void markMessageAsRead(Integer messageId);
    void deleteMessage(Integer messageId);
    long getUnreadMessageCount(Integer userId);
} 