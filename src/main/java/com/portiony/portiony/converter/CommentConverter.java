package com.portiony.portiony.converter;

import com.portiony.portiony.dto.comment.CreateCommentRequest;
import com.portiony.portiony.dto.comment.CreateCommentResponse;
import com.portiony.portiony.dto.comment.CommentUserDTO;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostComment;
import com.portiony.portiony.entity.User;
import org.hibernate.Hibernate;


public class CommentConverter {
    public static PostComment toPostCommentEntity(Post post, User user, CreateCommentRequest request) {
        return PostComment.builder()
                .post(post)
                .user(user)
                .content(request.getContent())
                .build();
    }

    public static CreateCommentResponse toCreateCommentsResponse(PostComment postComment) {
        User user = postComment.getUser();

        return CreateCommentResponse.builder()
                .id(postComment.getId())
                .content(postComment.getContent())
                .createdAt(postComment.getCreatedAt())
                .commentUser(CommentUserDTO.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImage())
                        .build())
                .build();
    }

}