package com.example.tomatomall.service;

import com.example.tomatomall.po.Message;
import com.example.tomatomall.vo.MessageVO;

import java.util.List;

public interface MessageService {
    String sendMessage(MessageVO message);
    List<MessageVO> getMessagesByToUserId(Integer userId);
    List<MessageVO> getMessagesByFromUserId(Integer userId);
    void markMessageAsRead(Integer messageId);
    void deleteMessage(Integer messageId);
    long getUnreadMessageCount(Integer userId);
    Integer queryMessageCount(Integer fromUserId , String content);
} 