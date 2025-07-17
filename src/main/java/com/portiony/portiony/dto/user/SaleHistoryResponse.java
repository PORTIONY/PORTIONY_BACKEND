package com.portiony.portiony.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SaleHistoryResponse {
    private Long postId;
    private String title;
    private int price;
    private String thumbnail;
    private String region;
    private int daysLeft;
    private LocalDateTime createdAt;
    private String status;
}
