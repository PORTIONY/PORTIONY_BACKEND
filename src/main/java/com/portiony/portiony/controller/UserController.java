package com.portiony.portiony.controller;

import com.portiony.portiony.dto.KakaoSignupRequestDto;
import com.portiony.portiony.dto.LoginRequestDto;
import com.portiony.portiony.dto.LoginResponseDto;
import com.portiony.portiony.dto.SignupRequestDto;
import com.portiony.portiony.dto.common.PageResponse;
import com.portiony.portiony.dto.user.*;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.entity.enums.PostStatus;
import com.portiony.portiony.repository.UserRepository;
import com.portiony.portiony.security.CustomUserDetails;
import com.portiony.portiony.service.UserService;
import com.portiony.portiony.util.JwtUtil;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // 이메일 중복 체크
    @GetMapping("/signup/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    // 닉네임 중복 체크
    @GetMapping("/signup/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean exists = userService.existsByNickname(nickname);
        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }

    // 회원가입 (기존 REST)
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto dto) {
        userService.signup(dto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    // 일반 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto response = userService.login(requestDto);
        return ResponseEntity.ok(response);
    }

    // 카카오 로그인 성공 시 (OAuth2User 활용)
    @Hidden
    @GetMapping("/login/oauth/kakao/success")
    public ResponseEntity<?> kakaoLoginSuccess(@RequestParam("code") String code) {
        log.info("카카오 로그인 redirect code 수신: {}", code);

        Object result = userService.kakaoLogin(code);

        if (result instanceof LoginResponseDto loginResponse) {
            return ResponseEntity.ok(loginResponse); // 기존 회원 → 로그인완료
        } else if (result instanceof KakaoSignupRequestDto signupInfo) {
            return ResponseEntity.status(HttpStatus.OK).body(signupInfo); // 신규 회원 → 가입절차로 이동
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    // 카카오 신규로그인 경우 회원가입완료
    @Hidden
    @PostMapping("/login/oauth/kakao/signup")
    public ResponseEntity<String> kakaoSignup(@Valid @RequestBody KakaoSignupRequestDto dto) {
        userService.kakaoSignup(dto);
        return ResponseEntity.ok("카카오 회원가입이 완료되었습니다.");
    }

    // 사용자 프로필 조회
    @GetMapping("/")
    public UserProfileResponse getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getUserProfile(userDetails);
    }

    // 내 프로필 수정 화면 조회
    @GetMapping("/me")
    public EditProfileViewResponse getMyProfileForEdit(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.editProfileView(userDetails);
    }

    // 내 프로필 수정
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> editProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails, @ModelAttribute EditProfileRequest request) {
        userService.editProfile(userDetails, request);
        return ResponseEntity.ok(Collections.singletonMap("message", "프로필이 수정되었습니다."));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody DeleteUserRequest request) {
        userService.deleteUser(userDetails, request);
        return ResponseEntity.ok(Collections.singletonMap("message", "탈퇴가 완료되었습니다."));
    }

    // 내 구매 내역 조회
    @GetMapping("/me/purchases")
    public PageResponse<PurchaseHistoryResponse> getMyPurchases(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "recent") String dateSort,
            @RequestParam(required = false) String priceSort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return userService.getMyPurchases(userDetails, dateSort, priceSort, page, size);
    }

    // 판매 내역 조회 (특정 유저)
    @GetMapping("/{userId}/sales")
    public PageResponse<SaleHistoryResponse> getSales(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            @RequestParam(required = false) String dateSort,
            @RequestParam(required = false) String priceSort,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return userService.getMySales(userDetails, userId, dateSort, priceSort, status, page, size);
    }

    // 내가 작성한 후기 조회
    @GetMapping("/me/reviews")
    public PageResponse<ReviewHistoryResponse> getReviewsByMe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "recent") String reviewSort,
            @RequestParam(required = false) Boolean writtenStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.getReviewsByMe(userDetails, type, reviewSort, writtenStatus, page, size);
    }

    // 받은 후기 조회
    @GetMapping("/reviews/{userId}")
    public PageResponse<ReviewHistoryResponse> getReviewsByOther(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(required = false, defaultValue = "high") String starSort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        return userService.getReviewsByOther(userDetails, userId, type, sort, starSort, page, size);
    }

    // 찜 목록 조회
    @GetMapping("/me/wishlist")
    public PageResponse<PostLikeHistoryResponse> getMyWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return userService.getWishlist(userDetails, sort, status, page, size);
    }

    // 후기 등록
    @PostMapping("/me/reviews/{chatRoomId}")
    public ResponseEntity<Map<String, String>> registerReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatRoomId,
            @RequestBody @Valid ReviewRegisterRequest request
    ) {
      
        //후기등록 api 로그
        log.info("리뷰 등록 요청: reviewId={}, star={}, choice={}, content={}",
                chatRoomId, request.getStar(), request.getChoice(), request.getContent());

        userService.registerReview(userDetails, chatRoomId, request);
        return ResponseEntity.ok(Collections.singletonMap("message", "후기가 등록되었습니다."));
    }

    // 후기 삭제
    @DeleteMapping("/me/reviews/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chatRoomId
    ) {
        userService.deleteReview(userDetails, chatRoomId);
        return ResponseEntity.ok(Collections.singletonMap("message", "후기가 삭제되었습니다."));
    }

}
