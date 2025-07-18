package com.portiony.portiony.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class ChatResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRoomRsDTO{
        Long chatRoomId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageRsDTO{
        private Long messageId;
        private Long senderId;
        private String content;
        private boolean isRead;
        private LocalDateTime createdAt;
    }
}
