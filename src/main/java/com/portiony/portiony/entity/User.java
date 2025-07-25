package com.portiony.portiony.entity;

import com.portiony.portiony.entity.common.BaseEntity;
import com.portiony.portiony.entity.enums.UserRole;
import com.portiony.portiony.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import javax.management.Notification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 150)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "varchar(10) default 'USER'")
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "double default 0.0")
    private double star = 0.0;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int salesCount = 0;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int purchase_count = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subregion_id", nullable = false)
    private Subregion subregion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dong_id", nullable = false)
    private Dong dong;
























// 추후 양방향 매핑 고려를 위해 추가해 놓았으니 삭제하지 말아주세요!
// 사용자 선호 정보
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<UserPreference> preferenceList = new ArrayList<>();
//
//    //사용자 동의
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<UserAgreement> userAgreementList = new ArrayList<>();
//
//    //게시글
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Post> postList = new ArrayList<>();
//
//    // 찜한 게시글 목록 (PostLike)
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<PostLike> postLikeList = new ArrayList<>();
//
//    // 게시글 댓글
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<PostComment> postCommentList = new ArrayList<>();
//
//    // 게시글 댓글
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Notification> notiList = new ArrayList<>();
//
//    //채팅방
//    @OneToMany(mappedBy = "buyer")
//    private List<ChatRoom> buyingChatRooms = new ArrayList<>();
//
//    @OneToMany(mappedBy = "seller")
//    private List<ChatRoom> sellingChatRooms = new ArrayList<>();
//
//    //채팅 메시지
//    @OneToMany(mappedBy = "sender")
//    private List<ChatMessage> chatMessageList = new ArrayList<>();
//
//    //후기
//    @OneToMany(mappedBy = "writer")
//    private List<Review> reviewList = new ArrayList<>();
//
//    @OneToMany(mappedBy = "target")
//    private List<Review> reviewList = new ArrayList<>();

//    public void likePost(com.portiony.portiony.PostLike like) {
//        likedPosts.add(like);
//        like.setUser(this);
//    }
//
//    public void unlikePost(PostLike like) {
//        likedPosts.remove(like);
//        like.setUser(null);
//    }

//    public void addPreference(UserPreference preference) {
//        preferences.add(preference);
//        preference.setUser(this);
//    }
//
//    public void removePreference(UserPreference preference) {
//        preferences.remove(preference);
//        preference.setUser(null);
//    }
//
//    public void addUserAgreement(UserAgreement ua) {
//        userAgreements.add(ua);
//        ua.setUser(this);
//    }
//
//    public void removeUserAgreement(UserAgreement ua) {
//        userAgreements.remove(ua);
//        ua.setUser(null);
//    }

}
