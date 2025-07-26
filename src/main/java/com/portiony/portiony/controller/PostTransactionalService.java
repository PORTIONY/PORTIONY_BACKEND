package com.portiony.portiony.controller;

import com.portiony.portiony.converter.PostConverter;
import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostCategory;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.repository.PostRepository;
import com.portiony.portiony.security.CustomUserDetails;
import com.portiony.portiony.service.PostImageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostTransactionalService {
    private final PostRepository postRepository;
    private final PostImageService postImageService;
    /**
     * 게시글 및 이미지 메타 데이터 DB에 저장
     * @param userDetails 로그인 유저 (게시글 작성자)
     * @param request 작성한 게시글 데이터
     * @param urls 업로드한 파일 url
     * @return 저장된 post Entity id
     */
    @Transactional
    public Long savePostWithImages(CustomUserDetails userDetails, CreatePostRequest request, List<String> urls) {
        // 1. 게시글 DB 저장
        User currentUser = userDetails.getUser();
        PostCategory category = PostCategory.builder()
                .id(request.getCategoryId())
                .build();
        Post post = PostConverter.toPostEntity(request, currentUser, category);

        Post savedPost = postRepository.save(post);

        // 2. 이미지 메타 데이터 저장
        postImageService.saveNewPostImages(urls, savedPost);

        return savedPost.getId();
    }

    @Transactional
    public void deletePost(Long currentUserId, Long postId) {
        Post post = postRepository.findPostByIdAndUserId(postId, currentUserId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글이 없거나 권한이 없습니다."));

        // 현재는 완전 데이터 삭제가 아닌 게시글이 '삭제 상태'가 되도록 구현이 되어있으므로 미사용 -> 추가 개발시 이어서 개발합니다!
        // 1. 연관된 이미지 메타 데이터 삭제
        // postImageService.deletePostImagesByPost(post);

        // 2. 게시글 삭제
        post.delete();
    }
}
