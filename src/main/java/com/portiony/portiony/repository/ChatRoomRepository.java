package com.portiony.portiony.repository;

import com.portiony.portiony.dto.user.PurchaseProjectionDto;
import com.portiony.portiony.dto.user.SaleProjectionDto;
import com.portiony.portiony.entity.ChatRoom;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, JpaSpecificationExecutor<ChatRoom> {
    List<ChatRoom> findBySellerIdOrBuyerId(Long sellerId, Long buyerId);
    List<ChatRoom> findByBuyerId(Long buyerId);
    List<ChatRoom> findBySellerId(Long sellerId);

    Optional<ChatRoom> findByPostIdAndBuyerId(Long postId, Long buyerId);

//    @Query("SELECT new com.portiony.portiony.dto.user.PurchaseProjectionDto(" +
//           "cr.post.id, cr.post.title, cr.post.price," +
//           "cr.post.user.region.city, cr.post.deadline, cr.post.createdAt," +
//           "cr.post.status, cr.finishDate, cr.post.id) " +
//           "FROM ChatRoom cr " +
//           "WHERE cr.buyer.id = :userId")
//    Page<PurchaseProjectionDto> findPurchasesIncludePost(@Param("userId") Long userId, Pageable pageable);

    // 중복Count 방지로직 추가
    @Query(value = """
    SELECT DISTINCT new com.portiony.portiony.dto.user.PurchaseProjectionDto(
        cr.post.id, cr.id, cr.post.title, cr.post.price, 
        CONCAT(u.subregion.district, ' ', u.dong.dong),
        cr.post.deadline,
        cr.post.createdAt,
        cr.finishDate,
        cr.post.capacity
    )
    FROM ChatRoom cr JOIN cr.post p JOIN p.user u
    WHERE cr.buyer.id = :userId
    """,
    countQuery = """
    SELECT COUNT(DISTINCT cr.id)
    FROM ChatRoom cr
    WHERE cr.buyer.id = :userId
    """)
    Page<PurchaseProjectionDto> findPurchasesWithPost(@Param("userId") Long userId, Pageable pageable);

    // 중복Count 방지로직 추가
    // 구매자와 판매자 같은 아이디일 경우 방지 쿼리 추가 (2025.07.22)
    @Query(value = """
    SELECT DISTINCT new com.portiony.portiony.dto.user.SaleProjectionDto(
        cr.post.id, cr.id, cr.post.title, cr.post.price, 
        CONCAT(u.subregion.district, ' ', u.dong.dong),
        cr.post.deadline,
        cr.post.createdAt,
        cr.post.status,
        cr.finishDate,
        cr.post.capacity
    )
    FROM ChatRoom cr JOIN cr.post p JOIN p.user u
    WHERE cr.seller.id = :userId 
    AND cr.buyer.id <> :userId
    AND (:status IS NULL OR p.status = :status)
    
    """,
    countQuery = """
    SELECT COUNT(DISTINCT cr.id)
    FROM ChatRoom cr JOIN cr.post p
    WHERE cr.seller.id = :userId
        AND cr.buyer.id <> :userId
        AND (:status IS NULL OR cr.post.status = :status)
    """)
    Page<SaleProjectionDto> findSalesWithPost(@Param("userId") Long userId, @Param("status") PostStatus status, Pageable pageable);

    @Query("""
    SELECT cr.post.id, COUNT(cr)
    FROM ChatRoom cr
    WHERE cr.sellerStatus = 'COMPLETED' AND cr.buyerStatus = 'COMPLETED'
    GROUP BY cr.post.id
    """)
    List<Object[]> countCompletedByPostIdGrouped();

    @Query("""
    SELECT COUNT(cr)
    FROM ChatRoom cr
    WHERE cr.buyer.id = :userId
    AND cr.buyerStatus = 'COMPLETED'
    """)
    int countPurchases(@Param("userId") Long userId);

    @Query("""
    SELECT COUNT(cr)
    FROM ChatRoom cr
    WHERE cr.seller.id = :userId
    AND cr.sellerStatus = 'COMPLETED'
    """)
    int countSales(@Param("userId") Long userId);
}
