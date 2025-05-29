package com.example.tomatomall.po;

import com.example.tomatomall.enums.MessageType;
import javax.persistence.*;

import com.example.tomatomall.vo.MessageVO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Integer fromUser;

    @Column(nullable = false)
    private Integer toUser;


    @Column(nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    public MessageVO toVO() {
        MessageVO messageVO = new MessageVO();
        messageVO.setId(id);
        messageVO.setContent(content);
        messageVO.setMessageType(messageType);
        messageVO.setIsRead(isRead);
        messageVO.setFromUser(fromUser);
        messageVO.setToUser(toUser);
        messageVO.setCreatedTime(createdTime);
        return messageVO;

    }

} 