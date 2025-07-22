package com.portiony.portiony.repository;

import com.portiony.portiony.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long>, JpaSpecificationExecutor<PostCategory> {

}
