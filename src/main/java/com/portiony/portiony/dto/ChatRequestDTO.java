package com.portiony.portiony.dto;

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
        Long postId;

        @NotNull
        Long buyerId;
    }

    @Getter
    public static class ChatMessageDTO{
        private Long chatRoomId;
        private Long senderId; //추후 토큰에서 빼오기
        private String content;
    }
}

