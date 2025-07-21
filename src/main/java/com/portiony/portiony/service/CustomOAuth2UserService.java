package com.portiony.portiony.service;

import com.portiony.portiony.entity.User;
import com.portiony.portiony.repository.UserRepository;
import com.portiony.portiony.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("kakao_account.email");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // 신규 회원, 추가정보 입력 필요
            throw new RuntimeException("추가 정보 입력 필요");
        }

        // 기존 회원이면 JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getEmail());

        // JWT 토큰을 OAuth2User 속성에 추가해서 컨트롤러에서 꺼낼 수 있게 하자
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("accessToken", token);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                "id"  // 카카오 기본 식별자 필드명 (필요하면 변경)
        );
    }

}
