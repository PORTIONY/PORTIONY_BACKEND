package com.portiony.portiony.controller;

import com.portiony.portiony.dto.ChatRequestDTO;
import com.portiony.portiony.dto.ChatResponseDTO;
import com.portiony.portiony.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<ChatResponseDTO.CreateRoomRsDTO> createUser(
            @RequestBody @Valid ChatRequestDTO.CreateRoomRqDTO request){ //@PathVariable(name = "postId") Long postId){
        return ResponseEntity.ok(chatService.createChatRoom(request));
    }
}
