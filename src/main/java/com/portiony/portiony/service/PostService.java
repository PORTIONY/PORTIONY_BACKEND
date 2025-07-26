package com.portiony.portiony.service;

import com.portiony.portiony.controller.PostTransactionalService;
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
import com.portiony.portiony.repository.PostImageRepository;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final S3Uploader s3Uploader;
    private final PostTransactionalService postTransactionalService;
    private final PostImageRepository postImageRepository;

    /**
     * 이미지를 s3 서버에 업로드 하고 메타 데이터, 게시글 데이터를 DB에 저장
     * @param userDetails 로그인 유저 (게시글 작성자)
     * @param request 작성한 게시글 데이터
     * @param files 업로드한 파일 데이터
     * @return 저장된 post Entity id
     */

    public Long createPost(CustomUserDetails userDetails, CreatePostRequest request, List<MultipartFile> files) {
        // 1. 이미지 s3 업로드
        validatePostImageFiles(files);
        List<String> urls = s3Uploader.upload(files, "post");

        try {
            // 2. 게시글 및 메타 데이터 DB 저장
            return postTransactionalService.savePostWithImages(userDetails, request, urls);
        } catch (Exception e) {
            s3Uploader.deleteList(urls);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 업로드 중 오류 발생: ", e);
        }
    }


    public PostWithCommentsResponse getPostWithComments(Long postId) {
        Post post = postRepository.findPostById(postId)
                .orElseThrow(()->new IllegalArgumentException("게시글이 존재하지 않습니다."));
        Long likeCount = postLikeRepository.countByPostId(postId);
        List<String> postImage = postImageRepository.findImageUrlsByPostId(postId);

        PostDetailResponse postDetailResponse = PostConverter.toPostDetailResponse(post, likeCount, postImage);
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

    public void deletePost(Long postId, Long currentUserId) {
        postTransactionalService.deletePost(postId, currentUserId);
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

    /**
     * 이미지 업로드를 검증함
     * @param files 검증할 파일 리스트
     */
    public void validatePostImageFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지는 최소 1개 이상 업로드해야 합니다.");
        }

        if (files.size() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지는 최대 10개 등록 가능 합니다.");
        }

        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다.");
            }

            if (file.getSize() > 5 * 1024 * 1024) { //5MB 제한
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 크기는 5MB 이하만 가능합니다.");
            }
        }
    }
}
