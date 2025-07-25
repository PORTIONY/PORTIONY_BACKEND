package com.portiony.portiony.dto.user;

import com.portiony.portiony.entity.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class SaleHistoryResponse {
    private Long postId;
    private String title;
    private int price;
    private String thumbnail;
    private String region;
    private LocalDateTime createdAt;
    private String daysLeft;
    private PostStatus status;
    private String details;
}
