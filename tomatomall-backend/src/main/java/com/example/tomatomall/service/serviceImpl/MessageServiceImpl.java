package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Message;
import com.example.tomatomall.repository.MessageRepository;
import com.example.tomatomall.service.MessageService;
import com.example.tomatomall.vo.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    @Transactional
    public String sendMessage(MessageVO message) {
        messageRepository.save(message.toPO());
        return "消息成功发送";
    }

    @Override
    public List<MessageVO> getMessagesByToUserId(Integer userId) {
        // 收到的消息

        List<Message> messageList = messageRepository.findByToUserOrderByCreatedTimeDesc(userId);
        List<MessageVO> messageVOList = messageList.stream()
                .map(Message::toVO)
                .collect(Collectors.toList());
        return messageVOList;
    }
    @Override
    public List<MessageVO> getMessagesByFromUserId(Integer userId) {
        // 发送的消息
        List<Message> messageList = messageRepository.findByToUserOrderByCreatedTimeDesc(userId);
        List<MessageVO> messageVOList = messageList.stream()
                .map(Message::toVO)
                .collect(Collectors.toList());
        return messageVOList;
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