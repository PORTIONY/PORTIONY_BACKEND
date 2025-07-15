package com.portiony.portiony.repository;

import com.portiony.portiony.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatRoom, Long> {
}
