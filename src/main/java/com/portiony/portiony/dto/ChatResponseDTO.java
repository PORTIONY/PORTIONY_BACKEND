package com.portiony.portiony.dto;

import com.portiony.portiony.entity.enums.ChatStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponseDTO {
    //채팅방 생성
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRoomRsDTO{
        private Long chatRoomId;
    }

    //외부 스토리지에 업로드된 이미지 url들 응답
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatImageUploadRsDTO {
        private List<String> imageUrls;
    }

    //메시지 전송 응답
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageRsDTO{
        private Long chatRoomId;
        private Long messageId;
        private Long senderId;
        private String content;
        private List<String> imageUrls;
        private boolean isRead;
        private LocalDateTime createdAt;
    }

    //거래 완료 처리
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatCompleteRsDTO{
        private ChatStatus sellerStatus;
        private ChatStatus buyerStatus;
    }

    //메시지 대화 내역 조회
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMessageTotalListDTO{
        private Long chatRoomId;
        private List<GetMessageTotalRsDTO> messageList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMessageTotalRsDTO{
        private Long messageId;
        private Long senderId;
        private String content;
        private List<String> imageUrls; //이미지 경로 list > content랑 같이 보내지게 할 건지?
        private LocalDateTime createdAt;
        private Boolean isRead;
    }

    //채팅방 목록 조회
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDTO {
        private Long postId;
        private String title;
        private String imageUrl;
        private Integer price;
        private LocalDateTime deadline;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerDTO {
        private Long partnerId;
        private String name;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDTO {
        private ChatStatus sellerStatus;
        private ChatStatus buyerStatus;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomPreviewDTO {
        private Long chatRoomId;
        private PostDTO post;
        private PartnerDTO partner;
        private Long lastMessageSenderId;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
        private Boolean isRead;
        private StatusDTO status;
        private Boolean isSeller;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomListResponseDTO {
        private List<ChatRoomPreviewDTO> chatRoomsList;
    }





}
