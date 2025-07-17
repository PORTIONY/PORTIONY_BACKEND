package com.portiony.portiony.repository;

import com.portiony.portiony.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    @Query("SELECT pi.imageUrl FROM PostImage pi WHERE pi.post.id = :postId ORDER BY pi.orderNum ASC LIMIT 1")
    Optional<String> findThumbnailUrlByPostId(@Param("postId") Long postId);
}
