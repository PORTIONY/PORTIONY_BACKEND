package com.portiony.portiony.repository;

import com.portiony.portiony.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 폼 작성 중에 미리 검증해서 오류 줄이는 용도
    // 닉네임 존재 여부 확인
    boolean existsByNickname(String nickname); // 중복 확인용

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email); // 중복 확인용

    // 최종 signup 시점에서 중복검증을 다시하는 용도
    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자 조회
    Optional<User> findByNickname(String nickname);
}
