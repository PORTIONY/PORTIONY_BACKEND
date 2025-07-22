package com.portiony.portiony.controller;

import com.portiony.portiony.dto.Post.*;
import com.portiony.portiony.dto.comment.CommentListResponse;
import com.portiony.portiony.dto.comment.CreateCommentRequest;
import com.portiony.portiony.dto.comment.CreateCommentResponse;
import com.portiony.portiony.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping("/")
    public ResponseEntity<CreatePostResponse> createPost(@RequestBody CreatePostRequest request) {
        Long postId = postService.createPost(request);
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
    public ResponseEntity<CreateCommentResponse> createComments(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
        CreateCommentResponse response = postService.createComment(request, postId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<UpdatePostResponse> updatePost(@PathVariable Long postId, @RequestBody UpdatePostRequest request){
        UpdatePostResponse response = postService.updatePost(postId, request, 15L);
        return ResponseEntity.ok(response);
    }
}
