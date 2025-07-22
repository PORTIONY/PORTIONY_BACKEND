package com.portiony.portiony.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portiony.portiony.dto.LoginResponseDto;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.repository.UserRepository;
import com.portiony.portiony.service.*;
import com.portiony.portiony.security.*;
import com.portiony.portiony.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Swagger 요청 감지: Referer 또는 Accept 헤더로 판단
        String referer = request.getHeader("Referer");
        String accept = request.getHeader("Accept");

        if ((referer != null && referer.contains("/swagger")) ||
                (accept != null && accept.contains("text/html"))) {
            log.info("✅ Swagger 또는 HTML 요청은 OAuth2SuccessHandler에서 무시합니다.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // OAuth2 유저 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":401, \"message\":\"Kakao signup required\", \"email\":\"" + email + "\"}");
            return;
        }

        User user = userOptional.get();

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

        LoginResponseDto tokenResponse = new LoginResponseDto(accessToken, refreshToken);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
    }


}
