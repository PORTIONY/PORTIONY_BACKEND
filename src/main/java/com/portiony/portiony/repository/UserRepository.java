package com.portiony.portiony.repository;

import com.portiony.portiony.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname); // 닉네임 중복 체크용 메서드
}
