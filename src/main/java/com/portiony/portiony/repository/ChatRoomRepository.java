package com.portiony.portiony.repository;

import com.portiony.portiony.dto.user.PurchaseProjectionDto;
import com.portiony.portiony.dto.user.SaleProjectionDto;
import com.portiony.portiony.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, JpaSpecificationExecutor<ChatRoom> {

    @Query("SELECT new com.portiony.portiony.dto.user.PurchaseProjectionDto(" +
            "cr.post.id, cr.post.title, cr.post.price," +
            "cr.post.user.region.city, cr.post.deadline, cr.post.createdAt," +
            "cr.post.status, cr.finishDate, cr.post.id) " +
            "FROM ChatRoom cr " +
            "WHERE cr.buyer.id = :userId")
    Page<PurchaseProjectionDto> findPurchasesIncludePost(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.portiony.portiony.dto.user.SaleProjectionDto(" +
            "cr.post.id, cr.post.title, cr.post.price," +
            "cr.post.user.region.city, cr.post.deadline, cr.post.createdAt," +
            "cr.post.status) " +
            "FROM ChatRoom cr " +
            "WHERE cr.seller.id = :userId")
    Page<SaleProjectionDto> findSalesIncludePost(@Param("userId") Long userId, Pageable pageable);
}
