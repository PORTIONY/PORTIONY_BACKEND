package com.portiony.portiony.controller;

import com.portiony.portiony.dto.ChatRequestDTO;
import com.portiony.portiony.dto.ChatResponseDTO;
import com.portiony.portiony.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/message") // 클라이언트는 stomp /pub/chat/message 로 메시지 전송, 프론트에서 보내는 주소
    public void sendMessage(@Payload ChatRequestDTO.ChatMessageDTO messageDTO) {
        //db 저장
        ChatResponseDTO.ChatMessageRsDTO saved = chatService.saveMessage(messageDTO);
        //상대방에게 메시지 전달, /sub/chat/room/{chatRoomId}로 메시지 전송
        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + messageDTO.getChatRoomId(), saved);
    }
}

