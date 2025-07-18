package com.portiony.portiony.repository;

import com.portiony.portiony.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByPostIdAndBuyerId(Long postId, Long buyerId);
}
