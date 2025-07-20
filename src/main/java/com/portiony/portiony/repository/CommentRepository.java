package com.portiony.portiony.repository;

import com.portiony.portiony.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CommentRepository extends JpaRepository<PostComment, Long> {
}
