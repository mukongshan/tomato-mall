package com.example.tomatomall.controller;

import com.example.tomatomall.po.Message;
import com.example.tomatomall.service.MessageService;
import com.example.tomatomall.vo.MessageVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Response<String> sendMessage(@RequestBody MessageVO message) {
        return Response.buildSuccess(messageService.sendMessage(message));
    }

    @GetMapping("/received-list/{toUserId}")
    public Response<List<MessageVO>> getReceivedMessages(@PathVariable Integer toUserId) {
        List<MessageVO> messages = messageService.getMessagesByToUserId(toUserId);
        return Response.buildSuccess(messages);
    }

    @GetMapping("/sent-list/{fromUserId}")
    public Response<List<MessageVO>> getSentMessages(@PathVariable Integer fromUserId) {
        List<MessageVO> messages = messageService.getMessagesByFromUserId(fromUserId);
        return Response.buildSuccess(messages);
    }


    @PutMapping("/mark-read/{messageId}")
    public Response<String> markMessageAsRead(@PathVariable Integer messageId) {
        try {
            messageService.markMessageAsRead(messageId);
            return Response.buildSuccess("标记已读成功");
        } catch (Exception e) {
            return Response.buildError("404", e.getMessage());
        }
    }

    @DeleteMapping("/delete/{messageId}")
    public Response<String> deleteMessage(@PathVariable Integer messageId) {
        try {
            messageService.deleteMessage(messageId);
            return Response.buildSuccess("消息删除成功");
        } catch (Exception e) {
            return Response.buildError("400", e.getMessage());
        }
    }

    @GetMapping("/unread-count/{userId}")
    public Response<Long> getUnreadMessageCount(@PathVariable Integer userId) {
        long count = messageService.getUnreadMessageCount(userId);
        return Response.buildSuccess(count);
    }

    @GetMapping("/query")
    public Response<Integer> queryMessageCount(@RequestParam Integer fromUserId, @RequestParam String content) {
        return Response.buildSuccess(messageService.queryMessageCount(fromUserId, content));

    }
}