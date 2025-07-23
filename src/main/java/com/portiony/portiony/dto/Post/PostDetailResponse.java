package com.portiony.portiony.dto.Post;

import com.portiony.portiony.entity.enums.ChatStatus;
import com.portiony.portiony.entity.enums.DeliveryMethod;
import com.portiony.portiony.entity.enums.PostStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PostDetailResponse {
    private Long id;
    private Long categoryId;
    private String title;
    private String description;
    private int capacity;
    private int price;
    private String unit;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private PostStatus status;
    private DeliveryMethod deliveryMethod;
    private boolean isAgree;
    private List<String> images;
    private Long likes;
    private Long commentCount;
    private SellerDTO seller;
}
