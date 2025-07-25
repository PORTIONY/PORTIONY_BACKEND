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

    @Query(value = """
    SELECT new com.portiony.portiony.dto.user.PostLikeProjectionDto(
        p.id, p.title, p.price, 
        CONCAT(u.subregion.district, ' ', u.dong.dong),
        p.createdAt,
        p.deadline,
        p.status,
        pl.createdAt,
        p.capacity    
    )
    FROM PostLike pl JOIN pl.post p JOIN p.user u 
    LEFT JOIN PostImage pi ON pi.post = p
    WHERE pl.user.id = :userId
    AND (:status IS NULL OR p.status = :status)
    """,
    countQuery = """
    SELECT COUNT(DISTINCT p.id)
    FROM PostLike pl JOIN pl.post p
    WHERE pl.user.id = :userId
    AND (:status IS NULL OR p.status = :status)
    """)
    Page<PostLikeProjectionDto> findPostLikeWithRegion(@Param("userId") Long userId, @Param("status") PostStatus status, Pageable pageable);

    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
}
