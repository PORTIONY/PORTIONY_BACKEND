package com.portiony.portiony.repository;

import com.portiony.portiony.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자 조회
    Optional<User> findByNickname(String nickname); // 닉네임 중복 체크용

    // 닉네임 존재 여부 확인
    boolean existsByNickname(String nickname); // 닉네임 중복 확인용
}
