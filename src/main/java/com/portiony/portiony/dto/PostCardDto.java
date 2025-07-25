package com.portiony.portiony.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PostCardDto {
    private Long id;
    private String title;
    private int price;
    private String unit;
    private int capacity;
    private int completedCount;
    private String status;
    private LocalDate deadline;
    private String thumbnail;
}

