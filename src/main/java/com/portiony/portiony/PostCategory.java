package com.portiony.portiony;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post_category")
@Getter
@NoArgsConstructor
public class PostCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String title;

    // 🔁 양방향 연관관계: Category → Post
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addPost(Post post) {
        posts.add(post);
        post.setCategory(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setCategory(null);
    }
}
