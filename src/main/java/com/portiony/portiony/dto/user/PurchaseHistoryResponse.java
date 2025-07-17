package com.portiony.portiony.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PurchaseHistoryResponse {
    Long postId;
    String title;
    int price;
    String thumbnail;
    String region;
    int daysLeft;
    LocalDateTime purchasedAt;
    String status;
}
