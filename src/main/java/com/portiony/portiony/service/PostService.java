package com.portiony.portiony.service;

import com.portiony.portiony.converter.PostConverter;
import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.dto.Post.PostDetailResponse;
import com.portiony.portiony.dto.Post.PostWithCommentsResponse;
import com.portiony.portiony.dto.comment.CommentListResponse;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostCategory;
import com.portiony.portiony.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.portiony.portiony.repository.PostRepository;

import java.util.Collections;
import java.util.List;

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
        Post post = PostConverter.toPostEntity(request, currentUser, category);

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }


    public PostWithCommentsResponse getPostWithComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("게시글이 존재하지 않습니다."));
        PostDetailResponse postDetailResponse = PostConverter.toPostDetailResponse(post);

        //TODO : 추후 댓글 내용 리스트 불러오는 코드 추가
        List<CommentListResponse> CommentListResponse = Collections.emptyList();

        return new PostWithCommentsResponse(postDetailResponse,CommentListResponse);
    }
}
