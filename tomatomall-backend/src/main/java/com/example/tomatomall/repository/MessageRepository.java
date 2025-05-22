package com.example.tomatomall.repository;

import com.example.tomatomall.po.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByToUserOrderByCreatedTimeDesc(Integer toUser);
    List<Message> findByFromUserOrderByCreatedTimeDesc(Integer fromUser);
    long countByToUserAndIsReadFalse(Integer toUser);
} 