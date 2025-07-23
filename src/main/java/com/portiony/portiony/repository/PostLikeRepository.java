package com.portiony.portiony.repository;

import com.portiony.portiony.dto.user.PostLikeProjectionDto;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostLike;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserAndPostId(User user, Long postId);

    @Query("""
    SELECT new com.portiony.portiony.dto.user.PostLikeProjectionDto(
        pl.post.id, pl.post.title, pl.post.price, 
        CONCAT(s.district, ' ', d.dong),
        pl.post.createdAt,
        pl.post.deadline,
        pl.post.status,
        pl.createdAt,
        pl.post.id    
    )
    FROM PostLike pl JOIN pl.post p JOIN p.user u 
    JOIN Subregion s ON s.region = u.region
    JOIN Dong d ON d.subregion = s
    WHERE p.user.id = :userId
    AND (:status IS NULL OR p.status = :status)
    """)
    Page<PostLikeProjectionDto> findPostLikeWithRegion(@Param("userId") Long userId, @Param("status") PostStatus status, Pageable pageable);

    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
}
