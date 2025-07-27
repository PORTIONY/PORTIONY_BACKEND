package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "WHERE p.id = :postId AND p.isDeleted = false")
    Optional<Post> findPostById(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " +
            "WHERE p.id = :postId and p.user.id = :userId AND p.isDeleted = false")
    Optional<Post> findPostByIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    List<Post> findAllByIsDeletedFalseAndCategory_Id(Long categoryId);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findRecentPosts(Pageable pageable);

    // ✅ 카테고리 필터 추가됨
    @Query("SELECT p FROM Post p " +
            "WHERE p.isDeleted = false " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:keyword IS NULL OR p.title LIKE %:keyword%) " +
            "AND (:regionId IS NULL OR p.user.region.id = :regionId) " +
            "AND (:subregionId IS NULL OR p.user.subregion.id = :subregionId) " +
            "AND (:dongId IS NULL OR p.user.dong.id = :dongId) " +
            "AND (:categoryCode IS NULL OR p.category.code = :categoryCode)")
    Page<Post> findFilteredPosts(
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            @Param("regionId") Long regionId,
            @Param("subregionId") Long subregionId,
            @Param("dongId") Long dongId,
            @Param("categoryCode") String categoryCode,
            Pageable pageable
    );

    List<Post> findAllByIsDeletedFalseOrderByCreatedAtDesc();

    long countByIsDeletedFalse();
}
