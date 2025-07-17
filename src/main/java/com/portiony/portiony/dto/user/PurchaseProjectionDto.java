package com.portiony.portiony.dto.user;

import com.portiony.portiony.entity.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PurchaseProjectionDto {
    Long postId;
    String title;
    int price;
    String region;
    LocalDateTime deadline;
    LocalDateTime createdAt;
    PostStatus status;
    LocalDateTime purchasedAt;
    Long thumbnailPostId;
}
