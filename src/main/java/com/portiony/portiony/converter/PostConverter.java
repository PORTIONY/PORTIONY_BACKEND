package com.portiony.portiony.converter;

import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostCategory;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.entity.enums.DeliveryMethod;

public class PostConverter {
    /**
     * 게시글 등록 요청 DTO -> Post Entity
     * @param dto : 게시글 생성 요청 DTO
     * @param user : 작성자 (JWT 기반 유저)
     * @param category : DB 조회 카테고리
     * @return Post Entity
     */
    public static Post toEntity(CreatePostRequest dto, User user, PostCategory category) {
        return Post.builder()
                .user(user)
                .category(category)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .capacity(dto.getCapacity())
                .price(dto.getPrice())
                .unit(dto.getUnit())
                .deadline(dto.getDeadline())
                .deliveryMethod(DeliveryMethod.valueOf(dto.getDeliveryMethod()))
                .isAgree(dto.getIsAgree())
                .build();
    }
}
