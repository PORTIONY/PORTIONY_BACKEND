package com.portiony.portiony.service;

import com.portiony.portiony.converter.CommentConverter;
import com.portiony.portiony.converter.PostConverter;
import com.portiony.portiony.dto.Post.*;
import com.portiony.portiony.dto.comment.CommentDTO;
import com.portiony.portiony.dto.comment.CommentListResponse;
import com.portiony.portiony.dto.comment.CreateCommentRequest;
import com.portiony.portiony.dto.comment.CreateCommentResponse;
import com.portiony.portiony.entity.*;
import com.portiony.portiony.entity.enums.DeliveryMethod;
import com.portiony.portiony.repository.CommentRepository;
import com.portiony.portiony.repository.PostLikeRepository;
import com.portiony.portiony.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.portiony.portiony.repository.PostRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public Long createPost(CustomUserDetails userDetails, CreatePostRequest request) {
        User currentUser = userDetails.getUser();

        PostCategory category = PostCategory.builder()
                .id(request.getCategoryId())
                .build();

        //Post 생성
        Post post = PostConverter.toPostEntity(request, currentUser, category);

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }


    public PostWithCommentsResponse getPostWithComments(Long postId) {
        Post post = postRepository.findPostById(postId)
                .orElseThrow(()->new IllegalArgumentException("게시글이 존재하지 않습니다."));
        Long likeCount = postLikeRepository.countByPostId(postId);
        PostDetailResponse postDetailResponse = PostConverter.toPostDetailResponse(post, likeCount);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        CommentListResponse commentListResponse = getCommentsByPostId(postId, pageable);

        return new PostWithCommentsResponse(postDetailResponse,commentListResponse);
    }

    public CreateCommentResponse createComment(CustomUserDetails userDetails,
                                               Long postId,
                                               CreateCommentRequest request) {
        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        User currentUser = userDetails.getUser();

        PostComment postComment = CommentConverter.toPostCommentEntity(post, currentUser, request);
        PostComment saved = commentRepository.save(postComment);

        return CommentConverter.toCreateCommentsResponse(saved);
    }

    public CommentListResponse getCommentsByPostId(Long postId, Pageable pageable) {
        postRepository.findPostById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        Long totalCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
        postRepository.findPostById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        Page<CommentDTO> items = commentRepository.findAllByPostId(postId, pageable);

        return new CommentListResponse(totalCount, items);
    }

    @Transactional
    public UpdatePostResponse updatePost(Long postId,
                                         UpdatePostRequest request,
                                         Long currentUserId) {

        Post post = postRepository.findPostByIdAndUserId(postId, currentUserId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글이 없거나 권한이 없습니다."));
        DeliveryMethod method;
        try {
            method = DeliveryMethod.valueOf(request.getDeliveryMethod());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 배송 방법입니다.");
        }

        PostConverter.update(post, request, method);
        return new UpdatePostResponse();
    }

    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        Post post = postRepository.findPostByIdAndUserId(postId, currentUserId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글이 없거나 권한이 없습니다."));

        post.delete();
    }

    @Transactional
    public UpdatePostStatusResponse updateStatus(Long postId, Long currentUserId) {
        Post post = postRepository.findPostByIdAndUserId(postId, currentUserId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글이 없거나 권한이 없습니다."));
        post.updateStatus();
        return new UpdatePostStatusResponse();
    }

    @Transactional
    public LikePostResponse likePost(Long postId, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        if (postLikeRepository.existsByPostAndUser(post, user)) {
            throw new IllegalStateException("이미 찜한 게시글입니다.");
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .user(user)
                .build();
        postLikeRepository.save(postLike);

        return new LikePostResponse(true);
    }

    @Transactional
    public LikePostResponse unlikePost(Long postId, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        PostLike like = postLikeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new IllegalStateException("찜하지 않은 게시글입니다."));

        postLikeRepository.delete(like);

        return new LikePostResponse(false);
    }
}
