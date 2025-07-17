package com.portiony.portiony.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostLikeResponse {
    private Long postId;
    private String title;
    private int price;
    private String thumbnail;
    private String region;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private String status;
}
