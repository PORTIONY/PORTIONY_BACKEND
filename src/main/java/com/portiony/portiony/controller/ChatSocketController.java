package com.portiony.portiony.controller;

import com.portiony.portiony.dto.Chat.ChatRequestDTO;
import com.portiony.portiony.dto.Chat.ChatResponseDTO;
import com.portiony.portiony.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/message") // 클라이언트는 /pub/chat/message 로 메시지 전송
    public void sendMessage(ChatRequestDTO.ChatMessageDTO messageDTO) {
        ChatResponseDTO.ChatMessageRsDTO saved = chatService.saveMessage(messageDTO);
        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + messageDTO.getChatRoomId(), saved);
    }
}

