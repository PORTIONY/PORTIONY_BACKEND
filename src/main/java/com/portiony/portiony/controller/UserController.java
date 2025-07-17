package com.portiony.portiony.controller;

import com.portiony.portiony.dto.common.PageResponse;
import com.portiony.portiony.dto.user.*;
import com.portiony.portiony.entity.User;
import com.portiony.portiony.repository.UserRepository;
import com.portiony.portiony.service.UserService;
import com.portiony.portiony.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    //이메일 추출 후 파싱 메서드
    public Long extrctUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        return user.getId();
    }
    
    //TODO: 매개변수 추후 JWT 인증구현 후 UserDetailsImpl 클래스명 교체 필요함.
    @GetMapping("/")
    public UserProfileResponse getUserProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = extrctUserIdFromToken(authHeader);

        return userService.getUserProfile(userId);
    }

    @GetMapping("/me")
    public EditProfileViewResponse getMyProfileForEdit(@RequestHeader("Authorization") String authHeader) {
        Long userId = extrctUserIdFromToken(authHeader);

        return userService.editProfileView(userId);
    }

    @PatchMapping("/me")
    public ResponseEntity<Map<String, String>> editProfile(@RequestHeader("Authorization") String authHeader, @RequestBody EditProfileRequest request) {
        Long userId = extrctUserIdFromToken(authHeader);
        userService.editProfile(userId, request);

        return ResponseEntity.ok(Collections.singletonMap("message", "프로필이 수정되었습니다."));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestHeader("Authorization") String authHeader, @RequestBody DeleteUserRequest request) {
        Long userId = extrctUserIdFromToken(authHeader);
        return ResponseEntity.ok(Collections.singletonMap("message", "탈퇴가 완료되었습니다."));
    }

    @GetMapping("/me/purchases")
    public PageResponse<PurchaseHistoryResponse> getMyPurchases(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(required = false) String priceOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long userId = extrctUserIdFromToken(authHeader);
        return userService.getMyPurchases(userId, sort, priceOrder, page, size);
    }

    @GetMapping("/{userId}/sales")
    public PageResponse<SaleHistoryResponse> getSales(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String priceOrder,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ){
        Long myId = extrctUserIdFromToken(authHeader);
        return userService.getMySales(myId, userId, sort, priceOrder, status, page, size);
    }

    @GetMapping("me/reviews")
    public PageResponse<ReviewHistoryResponse> getReviewsByMe(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = extrctUserIdFromToken(authHeader);
        return userService.getReviewsByMe(userId, type, sort, status, page, size);
    }

    @GetMapping("reviews/{userId}")
    public PageResponse<ReviewHistoryResponse> getReviewsByOther(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String starSort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Long myId = extrctUserIdFromToken(authHeader);
        return userService.getReviewsByOther(myId, userId, type, sort, starSort, page, size);
    }

    @GetMapping("/me/wishlist")
    public PageResponse<PostLikeResponse> getMyWishlist(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long userId = extrctUserIdFromToken(authHeader);
        return userService.getWishlist(userId, sort, status, page, size);
    }

    @PostMapping("/me/reviews/{reviewId}")
    public ResponseEntity<Map<String, String>> registerReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewRegisterRequest request
        ) {
            Long userId = extrctUserIdFromToken(authHeader);
            userService.registerReview(userId, reviewId, request);
            return ResponseEntity.ok(Collections.singletonMap("message", "후기가 등록되었습니다."));
    }

    @DeleteMapping("/me/reviews/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reviewId
    ) {
        Long userId = extrctUserIdFromToken(authHeader);
        userService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(Collections.singletonMap("message", "후기가 삭제되었습니다.")));
    }
}
