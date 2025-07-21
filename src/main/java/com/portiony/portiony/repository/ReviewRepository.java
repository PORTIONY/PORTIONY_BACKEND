package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
        SELECT r FROM Review r JOIN r.chatRoom cr JOIN cr.post p
        WHERE r.writer.id = :userId
        AND (
            (:type = 'writer' AND r.writer.id = :userId) OR 
            (:type = 'target' AND r.target.id = :userId) 
        )
        AND (
            (:writtenStatus = true AND r.createdAt IS NOT NULL) OR 
            (:writtenStatus = false AND r.createdAt IS NULL)
        )
    """)
    Page<Review> findReviewsByMe(@Param("userId") Long userId, @Param("type") String type, @Param("writtenStatus") boolean writtenStatus, Pageable pageable);

    @Query("""
        SELECT r FROM Review r JOIN r.chatRoom cr JOIN cr.post p
        WHERE r.writer.id = :userId
        AND cr.status = 'COMPLETED'
        AND (
            (:writtenStatus = true AND r.createdAt IS NOT NULL) OR 
            (:writtenStatus = false AND r.createdAt IS NULL)
        )
    """)
    Page<Review> findReviewsByOther(@Param("userId") Long userId, @Param("type") String type, Pageable pageable);
}
