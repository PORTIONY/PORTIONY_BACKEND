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
import com.portiony.portiony.service.UserService;
import com.portiony.portiony.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
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
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // 이메일로 userId 추출 메서드 (토큰에서 이메일 추출 후 DB 조회)
    public Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        return user.getId();
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
    public ResponseEntity<Map<String, Object>> kakaoLoginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String token = (String) oAuth2User.getAttribute("accessToken");
        String email = (String) oAuth2User.getAttribute("kakao_account.email");

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("email", email);

        return ResponseEntity.ok(response);
    }

    // 카카오 신규회원 가입
    @Hidden
    @PostMapping("/login/oauth/kakao/signup")
    public ResponseEntity<String> kakaoSignup(@Valid @RequestBody KakaoSignupRequestDto dto) {
        userService.kakaoSignup(dto);
        return ResponseEntity.ok("카카오 회원가입이 완료되었습니다.");
    }

    // 사용자 프로필 조회
    @GetMapping("/")
    public UserProfileResponse getUserProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromToken(authHeader);
        return userService.getUserProfile(userId);
    }

    // 내 프로필 수정 화면 조회
    @GetMapping("/me")
    public EditProfileViewResponse getMyProfileForEdit(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromToken(authHeader);
        return userService.editProfileView(userId);
    }

    // 내 프로필 수정
    @PatchMapping("/me")
    public ResponseEntity<Map<String, String>> editProfile(@RequestHeader("Authorization") String authHeader,
                                                           @RequestBody EditProfileRequest request) {
        Long userId = extractUserIdFromToken(authHeader);
        userService.editProfile(userId, request);
        return ResponseEntity.ok(Collections.singletonMap("message", "프로필이 수정되었습니다."));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody DeleteUserRequest request) {
        Long userId = extractUserIdFromToken(authHeader);
        userService.deleteUser(userId, request);
        return ResponseEntity.ok(Collections.singletonMap("message", "탈퇴가 완료되었습니다."));
    }

    // 내 구매 내역 조회
    @GetMapping("/me/purchases")
    public PageResponse<PurchaseHistoryResponse> getMyPurchases(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "recent") String dateSort,
            @RequestParam(required = false) String priceSort,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long userId = extractUserIdFromToken(authHeader);
        return userService.getMyPurchases(userId, dateSort, priceSort, status, page, size);
    }

    // 판매 내역 조회 (특정 유저)
    @GetMapping("/{userId}/sales")
    public PageResponse<SaleHistoryResponse> getSales(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestParam(required = false) String dateSort,
            @RequestParam(required = false) String priceSort,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long myId = extractUserIdFromToken(authHeader);
        return userService.getMySales(myId, userId, dateSort, priceSort, status, page, size);
    }

    // 내가 작성한 후기 조회
    @GetMapping("/me/reviews")
    public PageResponse<ReviewHistoryResponse> getReviewsByMe(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String reviewSort,
            @RequestParam(required = false) boolean writtenStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = extractUserIdFromToken(authHeader);
        return userService.getReviewsByMe(userId, type, reviewSort, writtenStatus, page, size);
    }

    // 받은 후기 조회
    @GetMapping("/reviews/{userId}")
    public PageResponse<ReviewHistoryResponse> getReviewsByOther(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestParam(required = false) boolean writtenStatus,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String starSort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Long myId = extractUserIdFromToken(authHeader);
        return userService.getReviewsByOther(myId, userId, writtenStatus, type, sort, starSort, page, size);
    }

    // 찜 목록 조회
    @GetMapping("/me/wishlist")
    public PageResponse<PostLikeHistoryResponse> getMyWishlist(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long userId = extractUserIdFromToken(authHeader);
        return userService.getWishlist(userId, sort, status, page, size);
    }

    // 후기 등록
    @PostMapping("/me/reviews/{reviewId}")
    public ResponseEntity<Map<String, String>> registerReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewRegisterRequest request
    ) {
      
        //후기등록 api 로그
        log.info("리뷰 등록 요청: reviewId={}, star={}, choice={}, content={}",
                reviewId, request.getStar(), request.getChoice(), request.getContent());

        Long userId = extractUserIdFromToken(authHeader);
        userService.registerReview(userId, reviewId, request);
        return ResponseEntity.ok(Collections.singletonMap("message", "후기가 등록되었습니다."));
    }

    // 후기 삭제
    @DeleteMapping("/me/reviews/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reviewId
    ) {
        Long userId = extractUserIdFromToken(authHeader);
        userService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(Collections.singletonMap("message", "후기가 삭제되었습니다."));
    }

}
