package com.portiony.portiony.repository;

import com.portiony.portiony.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRopository extends JpaRepository<ChatMessage, Long> {
}
