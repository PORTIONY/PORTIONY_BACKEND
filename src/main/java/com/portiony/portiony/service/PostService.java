package com.portiony.portiony.service;

import com.portiony.portiony.converter.CommentConverter;
import com.portiony.portiony.converter.PostConverter;
import com.portiony.portiony.dto.Post.*;
import com.portiony.portiony.dto.comment.CommentDTO;
import com.portiony.portiony.dto.comment.CommentListResponse;
import com.portiony.portiony.dto.comment.CreateCommentRequest;
import com.portiony.portiony.dto.comment.CreateCommentResponse;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostCategory;
import com.portiony.portiony.entity.PostComment;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.entity.enums.DeliveryMethod;
import com.portiony.portiony.repository.CommentRepository;
import com.portiony.portiony.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.portiony.portiony.repository.PostRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

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
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        CommentListResponse commentListResponse = getCommentsByPostId(postId, pageable);
        return new PostWithCommentsResponse(postDetailResponse,commentListResponse);
    }

    public CreateCommentResponse createComment(CreateCommentRequest request, Long postId) {
        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        //TODO : 더미 데이터 수정
        User user = userRepository.findById(15L)
                .orElseThrow(()-> new EntityNotFoundException("유저가 존재하지 않습니다."));

        PostComment postComment = CommentConverter.toPostCommentEntity(post, user, request);
        PostComment saved = commentRepository.save(postComment);

        return CommentConverter.toCreateCommentsResponse(saved);
    }

    public CommentListResponse getCommentsByPostId(Long postId, Pageable pageable) {
        Long totalCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);
        List<CommentDTO> items = commentRepository.findAllByPostId(postId, pageable);
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
}
