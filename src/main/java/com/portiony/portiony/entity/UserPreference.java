package com.portiony.portiony.entity;

import com.portiony.portiony.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주로 찾는 상품 카테고리 (정수로 매핑)
    @Column(name = "main_category")
    private Integer mainCategory;

    // 구매 이유
    @Column(name = "purchase_reason")
    private Integer purchaseReason;

    // 구매 상황
    @Column(name = "situation")
    private Integer situation;

    // 생성 시각
    @Override
    @Column(name = "created_at", updatable = false)
    public LocalDateTime getCreatedAt() {
        return super.getCreatedAt();
    }

    @Override
    @Column(name = "updated_at")
    public LocalDateTime getUpdatedAt() {
        return super.getUpdatedAt();
    }

    // 사용자 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 연관관계 편의 메서드
//    public void setUser(User user) {
//        this.user = user;
//        if (!user.getPreferences().contains(this)) {
//            user.getPreferences().add(this);
//        }
//    }
}
