package com.portiony.portiony.controller;

import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.dto.Post.CreatePostResponse;
import com.portiony.portiony.dto.Post.PostWithCommentsResponse;
import com.portiony.portiony.dto.comment.CreateCommentRequest;
import com.portiony.portiony.dto.comment.CreateCommentResponse;
import com.portiony.portiony.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    //TODO : 댓글 구현 후 서비스에서 ResponseDTO 넘기도록 수정
    @PostMapping("/")
    public ResponseEntity<CreatePostResponse> createPost(@RequestBody CreatePostRequest request) {
        Long postId = postService.createPost(request);
        return ResponseEntity.ok(new CreatePostResponse(postId));
    }

    /**
     * 게시글 상세 정보 및 첫 페이지 댓글
     */
    //TODO : page 댓글 페이지네이션
    @GetMapping("/{postId}")
    public ResponseEntity<PostWithCommentsResponse> getPost(@PathVariable Long postId) {
        PostWithCommentsResponse response = postService.getPostWithComments(postId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CreateCommentResponse> createComments(@PathVariable Long postId, @RequestBody CreateCommentRequest request) {
        CreateCommentResponse response = postService.createComment(request, postId);
        return ResponseEntity.ok(response);
    }
}
