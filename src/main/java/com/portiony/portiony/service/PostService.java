package com.portiony.portiony.service;

import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostCategory;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.entity.enums.DeliveryMethod;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.portiony.portiony.repository.PostRepository;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    @Transactional
    public Long createPost(CreatePostRequest request) {
        // 더미 유저
        User currentUser = User.builder()
                .id(15L)
                .build();

        // 더미 카테고리
        PostCategory category = PostCategory.builder()
                .id(request.getCategoryId())
                .build();

        //Post 생성
        Post post = Post.builder()
                .user(currentUser)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .price(request.getPrice())
                .unit(request.getUnit())
                .deadline(request.getDeadline())
                .deliveryMethod(DeliveryMethod.valueOf(request.getDeliveryMethod())) // enum 변환
                .isAgree(request.getIsAgree())
                .build();

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }
}
