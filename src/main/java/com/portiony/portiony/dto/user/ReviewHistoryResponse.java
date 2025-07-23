package com.portiony.portiony.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ReviewHistoryResponse {
    private Long postId;
    private Long reviewId;
    private Boolean isWritten;
    private String title;
    private String type; // "sale" or "purchase"
    private LocalDateTime transactionDate;
    private Integer choice; //null 가능
    private String content; //null 가능
}
