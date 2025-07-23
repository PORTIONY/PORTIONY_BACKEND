package com.portiony.portiony.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ChatRequestDTO {
    //채팅방 생성 > 채팅하기 버튼 눌렀을 때 > path변수로 변경
//    @Getter
//    public static class CreateRoomRqDTO{
//        @NotNull
//        private Long postId;
//
////        @NotNull
////        private Long buyerId;
//    }

    //웹 소켓에 이미지 전송 전 rest api로 이미지 먼저 저장
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ChatImageUploadDTO {
        @NotNull
        private List<MultipartFile> images;
    }


    //웹 소켓 메시지 전송용 (이미지가 있다면 미리 위에 있는 dto 사용한.. rest api로 처리하여 images에 링크만 넣어 보냄)
    @Getter
    public static class ChatMessageDTO{
        private Long chatRoomId;
        private Long senderId; //추후 토큰에서 빼오기
        private String content;
        private List<String> imageUrls;
    }

}

