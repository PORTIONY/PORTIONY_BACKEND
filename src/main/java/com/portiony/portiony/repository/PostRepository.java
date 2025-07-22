package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제되지 않은 모든 게시물
    List<Post> findAllByIsDeletedFalse();

    // 삭제되지 않고, 특정 카테고리(title)에 해당하는 게시물
    List<Post> findAllByIsDeletedFalseAndCategory_Title(String title);
}
