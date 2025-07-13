package com.portiony.portiony.entity;

import com.portiony.portiony.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_comment")
@Getter
@NoArgsConstructor
public class PostComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔁 게시글(Post)과 양방향 ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_delete", nullable = false)
    private boolean isDeleted = false;

    // BaseEntity의 createdAt, updatedAt에 컬럼명 지정
    @Override
    @Column(name = "create_at", updatable = false)
    public java.time.LocalDateTime getCreatedAt() {
        return super.getCreatedAt();
    }

    @Override
    @Column(name = "updated_at")
    public java.time.LocalDateTime getUpdatedAt() {
        return super.getUpdatedAt();
    }

    // 연관관계 설정 메서드
    public void setPost(Post post) {
        this.post = post;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
