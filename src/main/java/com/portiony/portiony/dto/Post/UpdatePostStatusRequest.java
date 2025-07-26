package com.portiony.portiony.dto.Post;

import com.portiony.portiony.entity.enums.PostStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePostStatusRequest {
    private PostStatus status;
}