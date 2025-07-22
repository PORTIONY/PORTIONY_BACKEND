package com.portiony.portiony.dto.comment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDTO {
    Long commentId;
    CommentUserDTO commentUser;
    String content;
    LocalDateTime createdAt;
}
