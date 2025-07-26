package com.portiony.portiony.repository;

import com.portiony.portiony.dto.comment.CommentDTO;
import com.portiony.portiony.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;


public interface CommentRepository extends JpaRepository<PostComment, Long> {
    @Query("SELECT new com.portiony.portiony.dto.comment.CommentDTO( " +
                "c.id, " +
                "new com.portiony.portiony.dto.comment.CommentUserDTO(u.id, u.nickname, u.profileImage), " +
                "c.content, " +
                "c.createdAt" +
            ") " +
            "FROM PostComment c JOIN c.user u " +
            "WHERE c.post.id = :postId AND c.isDeleted = false")
    Page<CommentDTO> findAllByPostId(@Param("postId") Long postId, Pageable pageable);


    @Query("SELECT COUNT(c) FROM PostComment c WHERE c.post.id = :postId AND c.isDeleted = false")
    Long countByPostIdAndIsDeletedFalse(@Param("postId") Long postId);
}
