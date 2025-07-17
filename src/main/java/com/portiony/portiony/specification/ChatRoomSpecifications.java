package com.portiony.portiony.specification;

import com.portiony.portiony.entity.ChatRoom;
import com.portiony.portiony.entity.enums.PostStatus;
import org.springframework.data.jpa.domain.Specification;

public class ChatRoomSpecifications {

    public static Specification<ChatRoom> sellerIdEquals(Long sellerId) {
        return (root, query, builder) -> builder.equal(root.get("seller").get("id"), sellerId);
    }

    public static Specification<ChatRoom> postStatusEquals(PostStatus status) {
        return (root, query, builder) -> builder.equal(root.get("post").get("status"), status);
    }
}
