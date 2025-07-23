package com.portiony.portiony.dto.user;

import com.portiony.portiony.entity.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PurchaseProjectionDto {
    private Long postId;
    private String title;
    private int price;
    private String region;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private PostStatus status;
    private LocalDateTime purchasedAt;
    private Long thumbnailPostId;
}
