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

    //카카오로부터 사용자 정보 받아옴
    //DB에 해당 사용자가 없으면 새로 생성 (약관동의,위치설정,선호조사)
    //기존 사용자는 그대로 로그인

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("kakao_account.email");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
            attributes.put("email", email);
            attributes.put("isNewUser", true); // 추가 정보 입력 필요 표시

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_GUEST")),
                    attributes,
                    "id"
            );
        }

        // 기존 회원이면 JWT 토큰 생성
        String token = jwtUtil.generateAccessToken(user.getEmail());


        // JWT 토큰을 OAuth2User 속성에 추가해서 컨트롤러에서 꺼낼수있음
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("accessToken", token);
        attributes.put("email", email);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                "id"  // 카카오 기본 식별자 필드명 (필요하면 변경)
        );
    }

}
