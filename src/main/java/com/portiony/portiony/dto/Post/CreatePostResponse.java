package com.portiony.portiony.dto.Post;

import lombok.Getter;

@Getter
public class CreatePostResponse {
    private Long id;
    private String message = "게시글이 등록되었습니다.";

    public CreatePostResponse(Long id) {
        this.id = id;
    }
}