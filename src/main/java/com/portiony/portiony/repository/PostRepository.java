package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    /**
     * 상세 게시글 조회
     * PostId를 매개변수로 받아 해당하는 Post와 연관된 User Entity를 반환
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "WHERE p.id = :postId AND p.isDeleted = false")
    Optional<Post> findPostById(@Param("postId") Long postId);

    /**
     * 게시글 업데이트를 위한 검증
     */
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "WHERE p.id = :postId and p.user.id = :userId AND p.isDeleted = false")
    Optional<Post> findPostByIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    List<Post> findAllByIsDeletedFalseAndCategory_Id(Long categoryId);

    // 최신순 게시글 목록 (삭제되지 않은 것만)
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findRecentPosts(Pageable pageable);

}