package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.enums.PostStatus;
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

    /**
     * 카테고리 기준 게시글 조회 (삭제되지 않은 것만)
     */
    List<Post> findAllByIsDeletedFalseAndCategory_Id(Long categoryId);

    /**
     * 최신순 게시글 조회
     */
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findRecentPosts(Pageable pageable);

    /**
     * 필터링 조건이 있는 게시글 조회
     */
    @Query("""
        SELECT p FROM Post p
        WHERE p.isDeleted = false
        AND (:status IS NULL OR p.status = :status)
        AND (:keyword IS NULL OR p.title LIKE %:keyword%)
        AND (:regionId IS NULL OR p.user.region.id = :regionId)
        AND (:subregionId IS NULL OR p.user.subregion.id = :subregionId)
        AND (:dongId IS NULL OR p.user.dong.id = :dongId)
    """)
    Page<Post> findFilteredPosts(
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            @Param("regionId") Long regionId,
            @Param("subregionId") Long subregionId,
            @Param("dongId") Long dongId,
            Pageable pageable
    );
}
