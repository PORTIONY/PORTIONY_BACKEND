package com.portiony.portiony.dto.Post;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {
    private Long categoryId;
    private String title;
    private String description;
    private Integer capacity;       // 공구인원
    private Integer price;
    private Integer unitAmount;     // 소분량
    private String unit;            // 소분 단위

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;

    private String deliveryMethod;
    private Boolean isAgree;
}
