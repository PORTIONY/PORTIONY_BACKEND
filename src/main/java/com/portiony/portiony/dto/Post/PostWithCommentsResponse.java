package com.portiony.portiony.dto.Post;

import com.portiony.portiony.dto.comment.CommentDTO;
import com.portiony.portiony.dto.comment.CommentListResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;


import java.util.List;

@AllArgsConstructor
@Getter
public class PostWithCommentsResponse {
    private PostDetailResponse post;
    private CommentListResponse comments;
}

