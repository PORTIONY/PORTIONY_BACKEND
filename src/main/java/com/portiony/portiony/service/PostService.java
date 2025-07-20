package com.portiony.portiony.service;

import com.portiony.portiony.converter.CommentConverter;
import com.portiony.portiony.converter.PostConverter;
import com.portiony.portiony.dto.Post.CreatePostRequest;
import com.portiony.portiony.dto.Post.PostDetailResponse;
import com.portiony.portiony.dto.Post.PostWithCommentsResponse;
import com.portiony.portiony.dto.comment.CommentListResponse;
import com.portiony.portiony.dto.comment.CreateCommentRequest;
import com.portiony.portiony.dto.comment.CreateCommentResponse;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostCategory;
import com.portiony.portiony.entity.PostComment;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.repository.CommentRepository;
import com.portiony.portiony.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.portiony.portiony.repository.PostRepository;
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

        //TODO : 추후 댓글 내용 리스트 불러오는 코드 추가
        List<CommentListResponse> CommentListResponse = null;

        return new PostWithCommentsResponse(postDetailResponse,CommentListResponse);
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
}
