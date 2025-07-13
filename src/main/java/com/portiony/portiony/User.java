package com.portiony.portiony;

import com.portiony.portiony.entity.common.BaseEntity;
import com.portiony.portiony.entity.enums.UserRole;
import com.portiony.portiony.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 지역 (Region 엔티티가 존재한다고 가정)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "profile_image", length = 100)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private double star = 0.0;

    // 🔁 찜한 게시글 목록 (PostLike)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likedPosts = new ArrayList<>();

    public void likePost(PostLike like) {
        likedPosts.add(like);
        like.setUser(this);
    }

    public void unlikePost(PostLike like) {
        likedPosts.remove(like);
        like.setUser(null);
    }

    // created_at, updated_at 컬럼 이름 지정
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPreference> preferences = new ArrayList<>();

    public void addPreference(UserPreference preference) {
        preferences.add(preference);
        preference.setUser(this);
    }

    public void removePreference(UserPreference preference) {
        preferences.remove(preference);
        preference.setUser(null);
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAgreement> userAgreements = new ArrayList<>();

    public void addUserAgreement(UserAgreement ua) {
        userAgreements.add(ua);
        ua.setUser(this);
    }

    public void removeUserAgreement(UserAgreement ua) {
        userAgreements.remove(ua);
        ua.setUser(null);
    }

}
