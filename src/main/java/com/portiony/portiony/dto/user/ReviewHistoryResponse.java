package com.portiony.portiony.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ReviewHistoryResponse {
    private Long postId;
    private Long reviewId;
    private boolean isWritten;
    private String title;
    private String type; // "sale" or "purchase"
    private LocalDate transactionDate;
    private Integer choice; //null 가능
    private String content; //null 가능
}
