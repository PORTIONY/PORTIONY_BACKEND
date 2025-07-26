package com.portiony.portiony.dto.Post;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CreatePostRequest {
    private Long categoryId;
    private String title;
    private String description;
    private Integer capacity;
    private Integer price;
    private String unit;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private LocalDateTime deadline;
    private String deliveryMethod;
    private Boolean isAgree;
}