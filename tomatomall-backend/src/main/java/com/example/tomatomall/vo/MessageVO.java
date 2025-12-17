package com.example.tomatomall.vo;

import com.example.tomatomall.po.Message;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
public class MessageVO {
    private Integer id;
    private String content;
    private Boolean isRead;
    private Integer fromUser;
    private Integer toUser;
    private LocalDateTime createdTime;

    public Message toPO() {
        Message message = new Message();
        message.setId(id);
        message.setContent(content);
        message.setIsRead(isRead);
        message.setFromUser(fromUser);
        message.setToUser(toUser);
        message.setCreatedTime(createdTime);
        return message;

    }









}
