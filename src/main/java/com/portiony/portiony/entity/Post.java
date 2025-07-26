package com.portiony.portiony.entity;

import com.portiony.portiony.entity.common.BaseEntity;
import com.portiony.portiony.entity.enums.PostStatus;
import com.portiony.portiony.entity.enums.DeliveryMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PostCategory category;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int capacity; //공구인원

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int unitAmount; //소분량

    @Column(nullable = false, length = 30)
    private String unit; // 소분단위

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PROGRESS'")
    private PostStatus status = PostStatus.PROGRESS;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false, length = 20, columnDefinition = "varchar(20) default 'ALL'")
    private DeliveryMethod deliveryMethod = DeliveryMethod.ALL;

    @Column(name = "is_agree", nullable = false)
    private boolean isAgree;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    public void update(String title, String description, int capacity, int price, int unitAmount,
                       String unit, LocalDateTime deadline, DeliveryMethod deliveryMethod){
        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.price = price;
        this.unitAmount = unitAmount; // 추가
        this.unit = unit;
        this.deadline = deadline;
        this.deliveryMethod = deliveryMethod;
    }

    public void delete(){
        this.isDeleted = true;
    }

    public PostStatus updateStatus() {
        if (this.status == PostStatus.PROGRESS) {
            this.status = PostStatus.DONE;
        } else if (this.status == PostStatus.DONE) {
            this.status = PostStatus.PROGRESS;
        }
        return this.status;
    }
    public PostStatus updateStatus(PostStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("PostStatus는 null일 수 없습니다.");
        }
        this.status = newStatus;
        return this.status;
    }

}
