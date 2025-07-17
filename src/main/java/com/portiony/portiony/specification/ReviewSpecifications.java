package com.portiony.portiony.specification;

import com.portiony.portiony.entity.ChatRoom;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.Review;
import com.portiony.portiony.entity.enums.ChatStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ReviewSpecifications {

    public static Specification<Review> filterReviewsByMe (Long userId, String type, String writtenStatus) {
        return (root, query, cb) -> {
            Join<Review, ChatRoom> chatRoomJoin = root.join("chatRoom");
            Join<ChatRoom, Post> postJoin = chatRoomJoin.join("post");

            Predicate completed = cb.equal(chatRoomJoin.get("status"), ChatStatus.COMPLETED);
            Predicate writtenByMe = cb.equal(root.get("writer").get("id"), userId);

            Predicate typePredicate = cb.conjunction();
            if ("purchase".equalsIgnoreCase(type)) {
                typePredicate = cb.equal(root.get("buyer").get("id"), userId);
            } else if ("sales".equalsIgnoreCase(type)) {
                typePredicate = cb.equal(root.get("seller").get("id"), userId);
            }

            Predicate writtenPredicate = cb.conjunction();
            if ("written".equalsIgnoreCase(writtenStatus)) {
                writtenPredicate = cb.isNotNull(root.get("createdAt"));
            } else if ("not_written".equalsIgnoreCase(writtenStatus)) {
                writtenPredicate = cb.isNull(root.get("createdAt"));
            }

            return cb.and(completed, writtenByMe, typePredicate, writtenPredicate);
        };
    }

    public static Specification<Review> filterReviewsByOther (Long userId, String type) {
        return (root, query, cb) -> {
            Join<Review, ChatRoom> chatRoomJoin = root.join("chatRoom");

            Predicate completed = cb.equal(chatRoomJoin.get("status"), ChatStatus.COMPLETED);
            Predicate notMe = cb.notEqual(root.get("writer").get("id"), userId);

            Predicate typePredicate = cb.conjunction();
            if("sales".equalsIgnoreCase(type)) {
                typePredicate = cb.equal(root.get("buyer").get("id"), userId);
            } else if("purchase".equalsIgnoreCase(type)) {
                typePredicate = cb.equal(root.get("seller").get("id"), userId);
            }

            return cb.and(completed, notMe, typePredicate);
        };
    }
}
