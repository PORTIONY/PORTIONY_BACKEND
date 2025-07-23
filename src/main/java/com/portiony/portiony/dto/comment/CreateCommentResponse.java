package com.portiony.portiony.dto.comment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class CreateCommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
}
