package com.portiony.portiony.repository;

import com.portiony.portiony.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
