package com.portiony.portiony.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CommentUserDTO {
    private Long userId;
    private String nickname;
    private String profileImage;
}