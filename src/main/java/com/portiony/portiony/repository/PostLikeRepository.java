package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostLikeRepository extends JpaRepository<PostLike, Long>, JpaSpecificationExecutor<PostLike> {
    boolean existsByUserAndPostId(Long userId, Long postId);
}
