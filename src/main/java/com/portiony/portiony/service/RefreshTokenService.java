package com.portiony.portiony.service;

import com.portiony.portiony.entity.RefreshToken;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.repository.RefreshTokenRepository;
import com.portiony.portiony.repository.UserRepository;
import com.portiony.portiony.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 리프레시 토큰 생성 및 저장
    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 토큰 삭제 + 즉시 반영
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString()) // UUID로 토큰 생성
                .expiryDate(LocalDateTime.now().plusDays(30)) // 일단 30일 유효기간
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // 리프레시 토큰 검증
    public boolean validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    // 리프레시 토큰으로 사용자 이메일 조회
    public String getEmailFromRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));
        return refreshToken.getUser().getEmail();
    }

    // 리프레시 토큰 삭제 (로그아웃 등)
    public void deleteRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        refreshTokenRepository.deleteByUser(user);
    }
}
