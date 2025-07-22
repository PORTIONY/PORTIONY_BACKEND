package com.portiony.portiony.repository;

import com.portiony.portiony.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdAndSenderIdNotAndIsReadFalse(Long chatRoomId, Long senderId);
    List<ChatMessage> findByChatRoomId(Long chatRoomId);
}
