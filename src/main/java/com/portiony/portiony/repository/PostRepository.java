package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
