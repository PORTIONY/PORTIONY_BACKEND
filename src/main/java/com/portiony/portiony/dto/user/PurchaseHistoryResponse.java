package com.portiony.portiony.dto.user;

import com.portiony.portiony.entity.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class PurchaseHistoryResponse {
    private Long postId;
    private Long chatRoomId;
    private String title;
    private int price;
    private String thumbnail;
    private String region;
    private LocalDateTime purchasedAt;
    private String daysLeft;
    private String details;
}
