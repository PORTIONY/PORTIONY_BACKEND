package com.portiony.portiony.controller;

import com.portiony.portiony.dto.ChatRequestDTO;
import com.portiony.portiony.dto.ChatResponseDTO;
import com.portiony.portiony.security.CustomUserDetails;
import com.portiony.portiony.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    //추후 모든 api에서 userid 삭제 > 토큰에서 빼올예정

    //메시지 대화 내역 조회
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ChatResponseDTO.GetMessageTotalListDTO> getMessageTotalList(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(chatService.getMessageTotalList(chatRoomId, userDetails.getUser().getId()));
    }

    //채팅방 목록 조회
    @GetMapping("")
    public ResponseEntity<ChatResponseDTO.ChatRoomListResponseDTO> getChatRoomList(
            @RequestParam(name = "type", defaultValue = "all") String type, //query 파라미터, 디폴트는 전체
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(chatService.getChatRoomList(userDetails.getUser().getId(), type));
    }

    //채팅방 생성
    @PostMapping("{postId}/room")
    public ResponseEntity<ChatResponseDTO.CreateRoomRsDTO> createUser(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ){
        return ResponseEntity.ok(chatService.createChatRoom(postId, userDetails.getUser().getId()));
    }

    //메시지 - 이미지 외부 스토리지에 업로드
    @PostMapping("/{chatRoomId}/images")
    public ResponseEntity<ChatResponseDTO.ChatImageUploadRsDTO> uploadImages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute ChatRequestDTO.ChatImageUploadDTO request
    ){
        List<String> imageUrls = chatService.uploadImages(chatRoomId, userDetails.getUser().getId(), request.getImages());
        return ResponseEntity.ok(
                ChatResponseDTO.ChatImageUploadRsDTO.builder()
                        .imageUrls(imageUrls)
                        .build()
        );
    }

    //메시지 읽음 처리
    @PatchMapping("/{chatRoomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        chatService.markMessagesAsRead(chatRoomId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    //거래 완료
    @PatchMapping("/{chatRoomId}/complete")
    public ResponseEntity<ChatResponseDTO.ChatCompleteRsDTO> chatToComplete(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(chatService.chatToComplete(chatRoomId, userDetails.getUser().getId()));
    }

}
