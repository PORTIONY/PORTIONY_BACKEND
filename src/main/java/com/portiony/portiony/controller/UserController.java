package com.portiony.portiony.controller;

import com.portiony.portiony.dto.*;
import com.portiony.portiony.service.UserService;
import com.portiony.portiony.dto.LoginRequestDto;
import com.portiony.portiony.dto.LoginResponseDto;
import com.portiony.portiony.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto dto) {
        userService.signup(dto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
    // 일반 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        LoginResponseDto response = userService.login(requestDto);
        return ResponseEntity.ok(response);
    }

    // 카카오 로그인
    @GetMapping("/login/oauth/kakao/success")
    public ResponseEntity<Map<String, Object>> kakaoLoginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User) {
        // OAuth2User에서 토큰과 이메일 정보 꺼내기
        String token = (String) oAuth2User.getAttribute("accessToken");
        String email = (String) oAuth2User.getAttribute("kakao_account.email");

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("email", email);

        return ResponseEntity.ok(response);
    }

    // 카카오로그인 - 신규사용자 추가정보 입력받기
    @PostMapping("/login/oauth/kakao/signup")
    public ResponseEntity<String> kakaoSignup(@RequestBody KakaoSignupRequestDto dto) {
        userService.kakaoSignup(dto);
        return ResponseEntity.ok("카카오 회원가입이 완료되었습니다.");
    }

}
