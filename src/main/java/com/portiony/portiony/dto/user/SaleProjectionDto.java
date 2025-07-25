package com.portiony.portiony.dto.user;

import com.portiony.portiony.entity.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SaleProjectionDto {
    private Long postId;
    private String title;
    private int price;
    private String region;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private PostStatus status;
    private LocalDateTime selledAt;
    private int capacity;
}
