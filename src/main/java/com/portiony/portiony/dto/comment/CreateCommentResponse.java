package com.portiony.portiony.dto.comment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
public class CreateCommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;

    private CommentUserDTO commentUser; // 추가
}
