package com.portiony.portiony.service;

import com.portiony.portiony.converter.PostConverter;
import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostCategory;
import com.portiony.portiony.entity.User;
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
        //TODO : 현재는 더미 데이터를 사용 중
        //후에 로그인한 사용자와 지정한 카테고리를 Repository로 조회해서 사용해야 함
        User currentUser = User.builder()
                .id(15L)
                .build();
        PostCategory category = PostCategory.builder()
                .id(request.getCategoryId())
                .build();

        //Post 생성
        Post post = PostConverter.toEntity(request, currentUser, category);

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }
}
