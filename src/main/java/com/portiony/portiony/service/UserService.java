package com.portiony.portiony.service;

import com.portiony.portiony.dto.*;
import com.portiony.portiony.entity.*;
import com.portiony.portiony.repository.*;
import com.portiony.portiony.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final SubregionRepository subregionRepository;
    private final DongRepository dongRepository;
    private final AgreementRepository agreementRepository;
    private final UserAgreementRepository userAgreementRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void signup(SignupRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.findByNickname(dto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
        Subregion subregion = subregionRepository.findById(dto.getSubregionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 하위 지역입니다."));
        Dong dong = dongRepository.findById(dto.getDongId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동 정보입니다."));

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        User user = User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .nickname(dto.getNickname())
                .profileImage(dto.getProfileImage())
                .region(region)
                .subregion(subregion)
                .dong(dong)
                .build();

        userRepository.save(user);

        // ✅ 사용자 선호정보 저장
        UserPreference preference = UserPreference.builder()
                .user(user)
                .mainCategory(dto.getMainCategory())
                .purchaseReason(dto.getPurchaseReason())
                .situation(dto.getSituation())
                .build();

        userPreferenceRepository.save(preference);  // ← 저장 필요!

        // 약관 동의 저장
        List<Agreement> agreements = agreementRepository.findAllById(dto.getAgreementIds());

        List<UserAgreement> userAgreements = new ArrayList<>();
        for (Agreement agreement : agreements) {
            userAgreements.add(UserAgreement.builder()
                    .user(user)
                    .agreement(agreement)
                    .isAgreed(true)
                    .build());
        }

        userAgreementRepository.saveAll(userAgreements);
    }



    // 일반 로그인
    public LoginResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponseDto(token);
    }

    // 카카오 회원가입
    public void kakaoSignup(KakaoSignupRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 지역, 서브지역, 동 정보 조회
        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
        Subregion subregion = subregionRepository.findById(dto.getSubregionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 하위 지역입니다."));
        Dong dong = dongRepository.findById(dto.getDongId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동 정보입니다."));

        // 사용자 엔티티 생성 및 저장
        User user = User.builder()
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .profileImage(dto.getProfileImage())
                .region(region)
                .subregion(subregion)
                .dong(dong)
                .build();

        userRepository.save(user);

        // 약관 동의 저장
        List<Agreement> agreements = agreementRepository.findAllById(dto.getAgreementIds());
        List<UserAgreement> userAgreements = new ArrayList<>();
        for (Agreement agreement : agreements) {
            userAgreements.add(UserAgreement.builder()
                    .user(user)
                    .agreement(agreement)
                    .isAgreed(true)
                    .build());
        }
        userAgreementRepository.saveAll(userAgreements);

        // 사용자 선호 정보 저장
        UserPreference preference = UserPreference.builder()
                .user(user)
                .mainCategory(dto.getMainCategory())
                .purchaseReason(dto.getPurchaseReason())
                .situation(dto.getSituation())
                .build();

        userPreferenceRepository.save(preference);
    }

}
