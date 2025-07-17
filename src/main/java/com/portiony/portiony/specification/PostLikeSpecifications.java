package com.portiony.portiony.specification;

import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.PostLike;
import com.portiony.portiony.entity.enums.PostStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PostLikeSpecifications {

    public static Specification<PostLike> filterByStatusAndUser(Long userId, String status) {
        return (root, query, cb) -> {
            Join<PostLike, Post> postJoin = root.join("post");

            Predicate byUser = cb.equal(postJoin.get("user").get("id"), userId);

            Predicate statusPredicate = cb.conjunction();
            if("ongoing".equalsIgnoreCase(status)) {
                statusPredicate = cb.equal(postJoin.get("status"), PostStatus.PROGRESS);
            } else if ("completed".equalsIgnoreCase(status)) {
                statusPredicate = cb.equal(postJoin.get("status"), PostStatus.DONE);
            }

            return cb.and(byUser, statusPredicate);
        };
    }
}
