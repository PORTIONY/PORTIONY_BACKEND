package com.portiony.portiony.dto.user;

import com.portiony.portiony.entity.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostLikeProjectionDto {
    private Long postId;
    private String title;
    private int price;
    private String region;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private PostStatus status;
    private LocalDateTime likedAt;
    private int capacity;
}
