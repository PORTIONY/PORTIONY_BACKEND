package com.portiony.portiony.entity;

import com.portiony.portiony.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agreement")
@Getter
@NoArgsConstructor
public class Agreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 약관 종류 (예: 이용약관, 개인정보 등)
    @Column(nullable = false, length = 50)
    private String type;

    // 약관 본문 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // created_at 설정
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

    // 사용자 동의 리스트 (양방향)
    @OneToMany(mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAgreement> userAgreements = new ArrayList<>();
}
