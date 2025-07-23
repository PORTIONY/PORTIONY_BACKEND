// TokenController.java 상단

package com.portiony.portiony.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import com.portiony.portiony.util.JwtUtil;
import com.portiony.portiony.service.RefreshTokenService;
import com.portiony.portiony.dto.RefreshRequestDto;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshRequestDto request) {
        String refreshToken = request.getRefreshToken();

        // 1. 존재하지 않음 or 만료
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "토큰 만료 상태거나 없습니다."));
        }

        try {
            String email = refreshTokenService.getEmailFromRefreshToken(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(email);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {

            // 2. 위조된경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "토큰 위조가 의심됩니다."));
        }
    }
}
