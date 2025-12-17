package com.example.tomatomall.controller;

import com.example.tomatomall.po.Message;
import com.example.tomatomall.service.MessageService;
import com.example.tomatomall.vo.MessageVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息管理控制器
 * 提供消息发送、接收、查询、标记已读等功能
 * 
 * @author TomatoMall Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送消息
     * 
     * @param message 消息内容
     * @return 发送结果
     */
    @PostMapping("/send")
    public Response<String> sendMessage(@RequestBody MessageVO message) {
        return Response.buildSuccess(messageService.sendMessage(message));
    }

    /**
     * 获取用户收到的消息列表
     * 
     * @param toUserId 接收用户ID
     * @return 收到的消息列表
     */
    @GetMapping("/received-list/{toUserId}")
    public Response<List<MessageVO>> getReceivedMessages(@PathVariable Integer toUserId) {
        List<MessageVO> messages = messageService.getMessagesByToUserId(toUserId);
        return Response.buildSuccess(messages);
    }

    /**
     * 获取用户发送的消息列表
     * 
     * @param fromUserId 发送用户ID
     * @return 发送的消息列表
     */
    @GetMapping("/sent-list/{fromUserId}")
    public Response<List<MessageVO>> getSentMessages(@PathVariable Integer fromUserId) {
        List<MessageVO> messages = messageService.getMessagesByFromUserId(fromUserId);
        return Response.buildSuccess(messages);
    }

    /**
     * 标记消息为已读
     * 
     * @param messageId 消息ID
     * @return 标记结果
     */
    @PutMapping("/mark-read/{messageId}")
    public Response<String> markMessageAsRead(@PathVariable Integer messageId) {
        try {
            messageService.markMessageAsRead(messageId);
            return Response.buildSuccess("标记已读成功");
        } catch (Exception e) {
            return Response.buildError("404", e.getMessage());
        }
    }

    /**
     * 删除消息
     * 
     * @param messageId 消息ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{messageId}")
    public Response<String> deleteMessage(@PathVariable Integer messageId) {
        try {
            messageService.deleteMessage(messageId);
            return Response.buildSuccess("消息删除成功");
        } catch (Exception e) {
            return Response.buildError("400", e.getMessage());
        }
    }

    /**
     * 获取用户未读消息数量
     * 
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @GetMapping("/unread-count/{userId}")
    public Response<Long> getUnreadMessageCount(@PathVariable Integer userId) {
        long count = messageService.getUnreadMessageCount(userId);
        return Response.buildSuccess(count);
    }

    /**
     * 查询消息数量
     * 
     * @param fromUserId 发送用户ID
     * @param content 消息内容
     * @return 消息数量
     */
    @GetMapping("/query")
    public Response<Integer> queryMessageCount(@RequestParam Integer fromUserId, @RequestParam String content) {
        return Response.buildSuccess(messageService.queryMessageCount(fromUserId, content));
    }
}