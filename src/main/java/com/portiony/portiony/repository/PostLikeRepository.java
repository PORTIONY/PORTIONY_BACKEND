package com.portiony.portiony.repository;

import com.portiony.portiony.entity.PostLike;
import com.portiony.portiony.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// 찜 내역 조회에 사용
public interface PostLikeRepository extends JpaRepository<PostLike, Long>, JpaSpecificationExecutor<PostLike> {
    boolean existsByUserAndPostId(User user, Long postId);
}
