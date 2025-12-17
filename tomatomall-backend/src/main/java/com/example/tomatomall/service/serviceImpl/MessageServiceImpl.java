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

/**
 * 消息服务实现类
 * 实现消息的发送、接收、查询、标记已读、删除等功能
 *
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@Service
public class MessageServiceImpl implements MessageService {

    // 注入消息数据访问层
    @Autowired
    private MessageRepository messageRepository;

    /**
     * 发送消息
     * @param message 消息VO对象
     * @return 发送结果
     */
    @Override
    @Transactional
    public String sendMessage(MessageVO message) {
        // 将VO对象转换为PO并保存到数据库
        messageRepository.save(message.toPO());
        return "消息成功发送";
    }

    /**
     * 获取用户收到的消息列表
     * @param userId 用户ID
     * @return 消息VO列表（按时间倒序）
     */
    @Override
    public List<MessageVO> getMessagesByToUserId(Integer userId) {
        // 查询以当前用户为接收方的所有消息，按创建时间倒序排列
        List<Message> messageList = messageRepository.findByToUserOrderByCreatedTimeDesc(userId);
        // 将PO对象转换为VO对象
        List<MessageVO> messageVOList = messageList.stream()
                .map(Message::toVO)
                .collect(Collectors.toList());
        return messageVOList;
    }

    /**
     * 获取用户发送的消息列表
     * @param userId 用户ID
     * @return 消息VO列表（按时间倒序）
     */
    @Override
    public List<MessageVO> getMessagesByFromUserId(Integer userId) {
        // 查询以当前用户为发送方的所有消息，按创建时间倒序排列
        List<Message> messageList = messageRepository.findByFromUserOrderByCreatedTimeDesc(userId);
        // 将PO对象转换为VO对象
        List<MessageVO> messageVOList = messageList.stream()
                .map(Message::toVO)
                .collect(Collectors.toList());
        return messageVOList;
    }

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     */
    @Override
    @Transactional
    public void markMessageAsRead(Integer messageId) {
        // 根据ID查找消息，不存在则抛异常
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        // 设置为已读
        message.setIsRead(true);
        // 保存更改
        messageRepository.save(message);
    }

    /**
     * 删除消息
     * @param messageId 消息ID
     */
    @Override
    @Transactional
    public void deleteMessage(Integer messageId) {
        // 根据ID删除消息
        messageRepository.deleteById(messageId);
    }

    /**
     * 获取用户未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @Override
    public long getUnreadMessageCount(Integer userId) {
        // 查询接收方为userId且未读的消息数量
        return messageRepository.countByToUserAndIsReadFalse(userId);
    }

    /**
     * 查询指定内容和发送方的未读消息数量
     * @param fromUserId 发送方ID
     * @param content 消息内容
     * @return 未读消息数量
     */
    @Override
    public Integer queryMessageCount(Integer fromUserId, String content) {
        // 查询指定发送方和内容且未读的消息列表
        List<Message> result = messageRepository.findByFromUserAndContentAndIsReadFalse(fromUserId, content);
        return result.size();
    }
}