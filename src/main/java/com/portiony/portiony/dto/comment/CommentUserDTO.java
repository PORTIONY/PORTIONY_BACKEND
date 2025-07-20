package com.portiony.portiony.dto.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentUserDTO {
    private Long userId;
    private String nickname;
    private String profileImage;
}
