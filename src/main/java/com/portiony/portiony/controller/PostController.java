package com.portiony.portiony.controller;

import com.portiony.portiony.dto.Post.*;
import com.portiony.portiony.dto.comment.CommentListResponse;
import com.portiony.portiony.dto.comment.CreateCommentRequest;
import com.portiony.portiony.dto.comment.CreateCommentResponse;
import com.portiony.portiony.security.CustomUserDetails;
import com.portiony.portiony.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreatePostResponse> createPost(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestPart("post") CreatePostRequest request,
        @RequestPart("images") List<MultipartFile> files){

        long postId = postService.createPost(userDetails, request, files);
        return ResponseEntity.ok(new CreatePostResponse(postId));
    }

    /**
     * 게시글 상세 정보 및 첫 페이지 댓글
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostWithCommentsResponse> getPost(@PathVariable Long postId) {
        PostWithCommentsResponse response = postService.getPostWithComments(postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<CommentListResponse> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getCommentsByPostId(postId, pageable));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CreateCommentResponse> createComments(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                @PathVariable Long postId,
                                                                @RequestBody CreateCommentRequest request) {
        CreateCommentResponse response = postService.createComment(userDetails, postId, request);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{postId}")
    public ResponseEntity<UpdatePostResponse> updatePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @PathVariable Long postId,
                                                         @RequestBody UpdatePostRequest request){
        UpdatePostResponse response = postService.updatePost(postId, request, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.deletePost(postId, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{postId}/status")
    public ResponseEntity<UpdatePostStatusResponse> updateStatus(
            @PathVariable Long postId,
            @RequestBody UpdatePostStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.updateStatus(postId, userDetails.getUser().getId(), request.getStatus()));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<LikePostResponse> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.likePost(postId, userDetails));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<LikePostResponse> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(postService.unlikePost(postId, userDetails));

    }
}
