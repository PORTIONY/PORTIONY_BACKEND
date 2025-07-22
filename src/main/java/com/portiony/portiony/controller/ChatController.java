package com.portiony.portiony.controller;

import com.portiony.portiony.dto.ChatRequestDTO;
import com.portiony.portiony.dto.ChatResponseDTO;
import com.portiony.portiony.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    //추후 모든 api에서 userid 삭제 > 토큰에서 빼올예정
    //채팅방 생성
    @PostMapping("/room")
    public ResponseEntity<ChatResponseDTO.CreateRoomRsDTO> createUser(
            @RequestBody @Valid ChatRequestDTO.CreateRoomRqDTO request){ //@PathVariable(name = "postId") Long postId){
        return ResponseEntity.ok(chatService.createChatRoom(request));
    }
    //메시지 - 이미지 외부 스토리지에 업로드
    @PostMapping("/{chatRoomId}/images/{userId}")
    public ResponseEntity<ChatResponseDTO.ChatImageUploadRsDTO> uploadImages(
            @PathVariable Long chatRoomId,
            @PathVariable Long userId,
            @ModelAttribute ChatRequestDTO.ChatImageUploadDTO request
    ){
        List<String> imageUrls = chatService.uploadImages(chatRoomId, userId, request.getImages());
        return ResponseEntity.ok(
                ChatResponseDTO.ChatImageUploadRsDTO.builder()
                        .imageUrls(imageUrls)
                        .build()
        );
    }
    //메시지 읽음 처리
    @PatchMapping("/{chatRoomId}/read/{userId}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long chatRoomId,
            @PathVariable Long userId
            //@AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //Long userId = userDetails.getUser().getId();
        chatService.markMessagesAsRead(chatRoomId, userId);
        return ResponseEntity.ok().build();
    }
    //거래 완료
    @PatchMapping("/{chatRoomId}/complete/{userId}")
    public ResponseEntity<ChatResponseDTO.ChatCompleteRsDTO> chatToComplete(
            @PathVariable Long chatRoomId,
            @PathVariable Long userId
            //@AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(chatService.chatToComplete(chatRoomId, userId));
    }
    //메시지 대화 내역 조회
    @GetMapping("/{chatRoomId}/messages/{userId}")
    public ResponseEntity<ChatResponseDTO.GetMessageTotalListDTO> getMessageTotalList(
            @PathVariable Long chatRoomId,
            @PathVariable Long userId
            //@AuthenticationPrincipal CustomUserDetails userDetails
    ){
        //Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(chatService.getMessageTotalList(chatRoomId, userId));
    }

}
