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
public class UserAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 동의 여부
    @Column(name = "is_agreed", nullable = false)
    private Boolean isAgreed;

    // 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 동의한 약관
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id", nullable = false)
    private Agreement agreement;

    // 편의 메서드
//    public void setUser(User user) {
//        this.user = user;
//        if (!user.getUserAgreements().contains(this)) {
//            user.getUserAgreements().add(this);
//        }
//    }
//
//    public void setAgreement(Agreement agreement) {
//        this.agreement = agreement;
//        if (!agreement.getUserAgreements().contains(this)) {
//            agreement.getUserAgreements().add(this);
//        }
//    }
}
