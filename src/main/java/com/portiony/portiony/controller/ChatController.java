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
    //추후 모든 api에서 userid 삭제 > 토큰에서 빼올예정
    @PostMapping("/room")
    public ResponseEntity<ChatResponseDTO.CreateRoomRsDTO> createUser(
            @RequestBody @Valid ChatRequestDTO.CreateRoomRqDTO request){ //@PathVariable(name = "postId") Long postId){
        return ResponseEntity.ok(chatService.createChatRoom(request));
    }

    @PostMapping("/{chatRoomId}/read/{userId}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long chatRoomId,
            @PathVariable Long userId
            //@AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //Long userId = userDetails.getUser().getId();
        chatService.markMessagesAsRead(chatRoomId, userId);
        return ResponseEntity.ok().build();
    }

}
