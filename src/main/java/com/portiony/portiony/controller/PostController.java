package com.portiony.portiony.controller;

import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.dto.Post.CreatePostResponse;
import com.portiony.portiony.dto.Post.PostWithCommentsResponse;
import com.portiony.portiony.dto.comment.CommentListResponse;
import com.portiony.portiony.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    //TODO : page 댓글 페이지네이션
    @GetMapping("/{postId}")
    public ResponseEntity<PostWithCommentsResponse> getPost(@PathVariable Long postId) {
        PostWithCommentsResponse response = postService.getPostWithComments(postId);
        return ResponseEntity.ok(response);
    }
}
