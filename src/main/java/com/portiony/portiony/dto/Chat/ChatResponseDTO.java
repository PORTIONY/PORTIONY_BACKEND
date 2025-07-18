package com.portiony.portiony.dto.Chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class ChatResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRoomRsDTO{
        Long chatRoomId;
    }

    @Getter
    public static class CreateRsDTO{
        private Long messageId;
        private Long senderId;
        private String content;
        private boolean isRead;
        private String createdAt;
    }
}
