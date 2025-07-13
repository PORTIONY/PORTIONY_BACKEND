package com.portiony.portiony.entity;

import com.portiony.portiony.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
}
