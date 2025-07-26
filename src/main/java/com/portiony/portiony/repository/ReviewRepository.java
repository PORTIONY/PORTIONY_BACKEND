package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Review;
import com.portiony.portiony.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByChatRoomIdAndWriterId(Long chatRoomId, Long writerId);

    @Query("""
    SELECT r FROM Review r JOIN r.chatRoom cr
    WHERE r.writer.id = :userId 
    AND (:type IS NULL OR :type = ''
        OR (:type = 'purchase'  AND cr.buyer.id  = :userId)
        OR (:type = 'sale' AND cr.seller.id = :userId))
    AND (:writtenStatus IS NULL
        OR (:writtenStatus = true  AND r.star <> 0.0)
        OR (:writtenStatus = false AND r.star = 0.0))
""")
    Page<Review> findAllReviewsByMe(@Param("userId") Long userId, @Param("type") String type,
                                    @Param("writtenStatus") Boolean writtenStatus, Pageable pageable);

    @Query( value = """
        SELECT r FROM Review r JOIN r.chatRoom cr
        WHERE r.target.id = :userId
        AND cr.sellerStatus = 'COMPLETED'
        AND cr.buyerStatus = 'COMPLETED'
        AND ( :type IS NULL OR :type = ''
        OR (:type = 'purchase' AND cr.buyer.id = :userId)
        OR (:type = 'sale' AND cr.seller.id = :userId)
        )
    """)
    Page<Review> findReviewsByOther(@Param("userId") Long userId, @Param("type") String type, Pageable pageable);
}
