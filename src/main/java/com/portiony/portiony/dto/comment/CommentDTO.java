package com.portiony.portiony.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CommentDTO {
    Long commentId;
    CommentUserDTO commentUser;
    String content;
    LocalDateTime createdAt;
}
