package com.portiony.portiony.repository;

import com.portiony.portiony.dto.user.PurchaseProjectionDto;
import com.portiony.portiony.dto.user.SaleProjectionDto;
import com.portiony.portiony.entity.ChatRoom;
import com.portiony.portiony.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>{

    @Query("""
    SELECT new com.portiony.portiony.dto.user.PurchaseProjectionDto(
        cr.post.id, cr.post.title, cr.post.price, 
        CONCAT(s.district, ' ', d.dong),
        cr.post.deadline,
        cr.post.createdAt,
        cr.post.status,
        cr.finishDate,
        cr.post.id    
    )
    FROM ChatRoom cr JOIN cr.post p JOIN p.user u 
    JOIN Subregion s ON s.region = u.region
    JOIN Dong d ON d.subregion = s
    WHERE cr.buyer.id = :userId
    AND (:status IS NULL OR p.status = :status)
    """)
    Page<PurchaseProjectionDto> findPurchasesWithPost(@Param("userId") Long userId, @Param("status") PostStatus status, Pageable pageable);

    @Query("""
    SELECT new com.portiony.portiony.dto.user.SaleProjectionDto(
        cr.post.id, cr.post.title, cr.post.price, 
        CONCAT(s.district, ' ', d.dong),
        cr.post.deadline,
        cr.post.createdAt,
        cr.post.status,
        cr.finishDate,
        cr.post.id    
    )
    FROM ChatRoom cr JOIN cr.post p JOIN p.user u 
    JOIN Subregion s ON s.region = u.region
    JOIN Dong d ON d.subregion = s
    WHERE p.user.id = :userId
    AND (:status IS NULL OR p.status = :status)
    """)
    Page<SaleProjectionDto> findSalesWithPost(@Param("userId") Long userId, @Param("status") PostStatus status, Pageable pageable);
}
