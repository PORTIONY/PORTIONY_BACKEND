package com.portiony.portiony.dto.Post;

import lombok.Getter;

@Getter
public class LikePostResponse {
    String message;
    boolean liked;

    public LikePostResponse(boolean liked) {
        this.liked = liked;
        this.message = liked ? "찜 등록 완료" : "찜 취소 완료";
    }
}