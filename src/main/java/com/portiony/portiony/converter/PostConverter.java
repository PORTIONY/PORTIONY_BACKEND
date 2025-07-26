package com.portiony.portiony.converter;

import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.dto.Post.PostDetailResponse;
import com.portiony.portiony.dto.Post.SellerDTO;
import com.portiony.portiony.dto.Post.UpdatePostRequest;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostCategory;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.entity.enums.DeliveryMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.portiony.portiony.entity.enums.PostStatus;


import java.util.List;

public class PostConverter {
    /**
     * 게시글 등록 요청 DTO를 Post Entity로 변환 합니다.
     *
     * @param dto 게시글 생성 요청 DTO
     * @param user 게시글 작성자
     * @param category 카테고리 Entity
     * @return Post Entity 객체
     */
    public static Post toPostEntity(CreatePostRequest dto, User user, PostCategory category) {
        DeliveryMethod deliveryMethod;
        try {
            deliveryMethod = DeliveryMethod.valueOf(dto.getDeliveryMethod()); // e.g., "DIRECT"
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 배송 방법입니다.");
        }

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
                .status(PostStatus.PROGRESS)  // ✅ 명시적으로 기본 상태 할당
                .build();
    }

    /**
     * 게시글 상세 정보에 필요한 Entity를 불러와 PostDetailResponse DTO로 변환합니다.
     * @param post 게시글 상세 정보 Entity
     * @param likeCount 찜 갯수
     * @param postImage 상품 이미지 url list
     * @return PostDetailResponse DTO 객체
     */
    public static PostDetailResponse toPostDetailResponse(Post post, Long likeCount, List<String> postImage) {
        SellerDTO seller = SellerDTO.builder()
                .sellerId(post.getUser().getId())
                .nickname(post.getUser().getNickname())
                .profileImage(post.getUser().getProfileImage())
                .saleCount(post.getUser().getSalesCount())
                .purchaseCount(post.getUser().getPurchase_count())
                .build();

        return PostDetailResponse.builder()
                .id(post.getId())
                .categoryId(post.getCategory().getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .capacity(post.getCapacity())
                .price(post.getPrice())
                .unit(post.getUnit())
                .deadline(post.getDeadline())
                .createdAt(post.getCreatedAt())
                .status(post.getStatus())
                .deliveryMethod(post.getDeliveryMethod())
                .isAgree(post.isAgree())
                .images(postImage)
                .likes(likeCount)
                .seller(seller)
                .build();
    }

    public static void update(Post post, UpdatePostRequest request, DeliveryMethod method){
        post.update(
                request.getTitle(),
                request.getDescription(),
                request.getCapacity(),
                request.getPrice(),
                request.getUnit(),
                request.getDeadline(),
                method
        );
    }
}
