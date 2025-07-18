package com.portiony.portiony.controller;

import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.dto.Post.CreatePostResponse;
import com.portiony.portiony.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
