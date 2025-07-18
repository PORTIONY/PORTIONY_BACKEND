package com.portiony.portiony.dto.Chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatRequestDTO {
    //채팅방 생성 > 채팅하기 버튼 눌렀을 때
    @Getter
    public static class CreateRoomRqDTO{
        @NotNull
        @NotBlank
        Long postId;

        @NotNull
        @NotBlank
        Long buyerId;
    }

    @Getter
    public static class CreateChatRqDTO{
        private Long chatRoomId;       // 어떤 방에서
        private Long senderId;         // 누가
        private String content;        // 무슨 말을
    }
}

