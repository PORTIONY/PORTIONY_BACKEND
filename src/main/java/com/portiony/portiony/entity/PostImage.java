package com.portiony.portiony.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    // 양방향 연관관계 (ManyToOne)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "post_id", nullable = false)
//    private Post post;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "order_num", nullable = false)
    private int orderNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

//    // 연관관계 편의 메서드
//    public void setPost(Post post) {
//        this.post = post;
//    }


    public PostImage(String imageUrl, int orderNum, Post post) {
        this.imageUrl = imageUrl;
        this.orderNum = orderNum;
        this.post = post;
    }
}
