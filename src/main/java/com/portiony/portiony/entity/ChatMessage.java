package com.portiony.portiony.entity;

import com.portiony.portiony.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String content;

    @Builder.Default
    @Column(name = "is_read", nullable = false, columnDefinition = "boolean default false")
    private boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Builder.Default
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL)
    private List<ChatImage> chatImageList = new ArrayList<>();

    //연관 관계 편의 메서드
    public void addChatImage(ChatImage image) {
        chatImageList.add(image);
        image.setChatMessage(this);
    }
}
