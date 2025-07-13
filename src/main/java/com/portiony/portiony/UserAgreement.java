package com.portiony.portiony;

import com.portiony.portiony.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_agreement")
@Getter
@NoArgsConstructor
public class UserAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 동의한 약관
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id", nullable = false)
    private Agreement agreement;

    // 동의 시각
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

    // 동의 여부
    @Column(name = "is_agreed", nullable = false)
    private boolean isAgreed;

    // 편의 메서드
    public void setUser(User user) {
        this.user = user;
        if (!user.getUserAgreements().contains(this)) {
            user.getUserAgreements().add(this);
        }
    }

    public void setAgreement(Agreement agreement) {
        this.agreement = agreement;
        if (!agreement.getUserAgreements().contains(this)) {
            agreement.getUserAgreements().add(this);
        }
    }
}
