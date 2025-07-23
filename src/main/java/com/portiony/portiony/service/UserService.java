package com.portiony.portiony.service;

import com.portiony.portiony.dto.*;
import com.portiony.portiony.dto.common.PageResponse;
import com.portiony.portiony.dto.user.*;
import com.portiony.portiony.entity.*;
import com.portiony.portiony.entity.enums.PostStatus;
import com.portiony.portiony.entity.enums.UserStatus;
import com.portiony.portiony.repository.*;
import com.portiony.portiony.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
    private final RefreshTokenService refreshTokenService;
    private final ChatRoomRepository chatRoomRepository;
    private final PostImageRepository postImageRepository;
    private final ReviewRepository reviewRepository;
    private final PostLikeRepository postLikeRepository;

    @Value("${kakao.rest-api-key}")
    private String kakaoClientId;

    // 공통 유저 생성 로직
    private User createUser(SignupBaseDto dto) {
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

        String encodedPassword = dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null;

        User user = User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .nickname(dto.getNickname())
                .profileImage(dto.getProfileImage())
                .region(region)
                .subregion(subregion)
                .dong(dong)
                .build();

        return userRepository.save(user);
    }

    private void saveAgreementsAndPreferences(SignupBaseDto dto, User user) {
        UserPreference preference = UserPreference.builder()
                .user(user)
                .mainCategory(dto.getMainCategory())
                .purchaseReason(dto.getPurchaseReason())
                .situation(dto.getSituation())
                .build();
        userPreferenceRepository.save(preference);

        List<Agreement> agreements = agreementRepository.findAllById(dto.getAgreementIds());
        List<UserAgreement> userAgreements = agreements.stream()
                .map(agreement -> UserAgreement.builder()
                        .user(user)
                        .agreement(agreement)
                        .isAgreed(true)
                        .build())
                .toList();
        userAgreementRepository.saveAll(userAgreements);
    }

    public LoginResponseDto signup(SignupRequestDto dto) {
        User user = createUser(dto);
        saveAgreementsAndPreferences(dto, user);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

        return new LoginResponseDto(accessToken, refreshToken);
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

        return new LoginResponseDto(accessToken, refreshToken);
    }

    public LoginResponseDto kakaoLogin(String code) {

        // 1. access token 요청
        String tokenUri = "https://kauth.kakao.com/oauth/token";

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId); // 카카오 REST API 키
        params.add("redirect_uri", "http://localhost:8080/api/users/login/oauth/kakao/success");
        params.add("code", code);

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<LinkedMultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, tokenHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUri, tokenRequest, Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "카카오 토큰 요청 실패");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. 사용자 정보 요청
        String userInfoUri = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);
        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(userInfoUri, HttpMethod.GET, userInfoRequest, Map.class);

        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "카카오 사용자 정보 요청 실패");
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoResponse.getBody().get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        // 3. 기존 사용자 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카카오 계정으로 가입된 사용자가 없습니다."));

        // 4. JWT access + refresh token 발급
        String jwtAccessToken = jwtUtil.generateAccessToken(user.getEmail());
        String jwtRefreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

        return new LoginResponseDto(jwtAccessToken, jwtRefreshToken);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }


    public KakaoSignupResponseDto kakaoSignup(KakaoSignupRequestDto dto) {
        User user = createUser(dto);
        saveAgreementsAndPreferences(dto, user);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

        return new KakaoSignupResponseDto(accessToken, refreshToken);
    }

    // 이메일 중복 확인
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 닉네임 중복 확인
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // 프로필 조회 (이메일로찾기)
    private User findUserById(Long userId) {
        return userRepository.findById(userId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"사용자를 찾을 수 없습니다."));
    }

    // 기본 정렬 (최신순/오래된순, 가격낮은순/가격높은순)
    // 정렬 병렬 적용.
    private Sort getSort(String dateSort, String priceSort) {
        Sort result = Sort.by(Sort.Direction.DESC, "post.createdAt");

        if ("recent".equals(dateSort)) {
            result = Sort.by(Sort.Direction.DESC, "post.createdAt");
        } else if ("oldest".equals(dateSort)) {
            result = Sort.by(Sort.Direction.ASC, "post.createdAt");
        }

        if ("asc".equals(priceSort)) {
            result = result.and(Sort.by(Sort.Direction.ASC, "post.price"));
        } else if ("desc".equals(priceSort)) {
            result = result.and(Sort.by(Sort.Direction.DESC, "post.price"));
        }

        return result;
    }

    // 리뷰 정렬
    // 정렬 병렬 적용
    private Sort getReviewSort(String starSort, String reviewSort) {
        Sort result = Sort.unsorted();

        if ("recent".equalsIgnoreCase(reviewSort)) {
            result = Sort.by(Sort.Direction.DESC, "chatRoom.finishDate");
        } else if ("oldest".equalsIgnoreCase(reviewSort)) {
            result = Sort.by(Sort.Direction.ASC, "chatRoom.finishDate");
        }

        if("high".equalsIgnoreCase(starSort)) {
            result = result.and(Sort.by(Sort.Direction.DESC, "star"));
        } else if ("low".equalsIgnoreCase(starSort)) {
            result = result.and(Sort.by(Sort.Direction.ASC, "star"));
        }

        return result;
    }

    // 찜 정렬
    // 정렬 단일 적용
    private Sort getWishlistSort(String postLikert) {
        switch (postLikert) {
            // 최신 찜순
            case "recent" :
                return Sort.by(Sort.Direction.DESC, "createdAt");

            //오래된 찜순
            case  "oldest" :
                return Sort.by(Sort.Direction.ASC, "createdAt");

            // 마감임박순
            case "deadline_asc":
                return Sort.by(Sort.Direction.ASC, "post.deadline");

            // 마감여유순
            case "deadline_desc":
                return Sort.by(Sort.Direction.DESC, "post.deadline");

            // 디폴트값은 오래된찜순
            default:
                return Sort.by(Sort.Direction.ASC, "createdAt");
        }
    }

    // 프로필 조회
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserById(userId);
        // TODO: userId 기반 유저 조회
        // TODO: 구매이력, 판매이력 수 계산 (주문/게시글 도메인 연동)
        // TODO: 긍정후기비율 계산 (리뷰 도메인 연동)

        return UserProfileResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .purchasesCount(user.getPurchase_count())
                .salesCount(user.getSalesCount())
                .positiveRate(user.getStar()) // 임시 값
                .isMine(true)
                .build();
    }

    // 프로필 편집 조회
    public EditProfileViewResponse editProfileView(Long userId) {
        User user = findUserById(userId);

        return EditProfileViewResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .build();
    }

    // 프로필 편집
    @Transactional
    public void editProfile(Long userId, EditProfileRequest request) {
        User user = findUserById(userId);

        //닉네임 수정하기 ("/signup/check-nickname" api 사용)
        //한번 더 검증하기
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }

        if (request.getNewPassword() != null || request.getCurrentPassword() != null) {
            if(request.getCurrentPassword() == null || request.getNewPassword() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"비밀번호 입력 시 현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"현재 비밀번호가 일치하지 않습니다.");
            }

            if(request.getCurrentPassword().equals(request.getNewPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"현재 비밀번호와 새로운 비밀번호가 일치합니다.");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(Long userId, DeleteUserRequest request) {
        User user = findUserById(userId);

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"비밀번호가 일치하지 않습니다.");
        }

        user.setStatus(UserStatus.WITHDRAWN);
    }

    // 내 구매 내역 조회
    public PageResponse<PurchaseHistoryResponse> getMyPurchases(Long userId, String dateSort, String priceSort, PostStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size, getSort(dateSort, priceSort));

        Page<PurchaseProjectionDto> purchases = chatRoomRepository.findPurchasesWithPost(userId, status, pageable);

        List<PurchaseHistoryResponse> content = purchases.getContent().stream()
                .map(dto -> {
                    String thumbnail = postImageRepository.findThumbnailUrlByPostId(dto.getPostId())
                            .orElse(null);

                    return PurchaseHistoryResponse.builder()
                            .postId(dto.getPostId())
                            .title(dto.getTitle())
                            .price(dto.getPrice())
                            .thumbnail(thumbnail)
                            .region(dto.getRegion())
                            //마감일 지나면 날짜가 음수로 이상하게 출력될 수 있을 것 같음. 추후 업데이트 고려하기.
                            .daysLeft((int) ChronoUnit.DAYS.between(LocalDateTime.now(), dto.getDeadline()))
                            .purchasedAt(dto.getCreatedAt())
                            .status(dto.getStatus())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageResponse<>(purchases.getTotalElements(), purchases.getNumber() + 1, content);
    }

    // 판매 내역 조회 (특정 유저)
    public PageResponse<SaleHistoryResponse> getMySales(Long myId, Long userId, String dateSort, String priceSort, PostStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size, getSort(dateSort, priceSort));

        Page<SaleProjectionDto> sales = chatRoomRepository.findSalesWithPost(userId, status, pageable);

        List<SaleHistoryResponse> content = sales.getContent().stream()
                .map(dto -> {
                    String thumbnail = postImageRepository.findThumbnailUrlByPostId(dto.getPostId())
                            .orElse(null);

                    return SaleHistoryResponse.builder()
                            .postId(dto.getPostId())
                            .title(dto.getTitle())
                            .price(dto.getPrice())
                            .thumbnail(thumbnail)
                            .region(dto.getRegion())
                            //마감일 지나면 날짜가 음수로 이상하게 출력될 수 있을 것 같음. 추후 업데이트 고려하기.
                            .daysLeft((int) ChronoUnit.DAYS.between(LocalDateTime.now(), dto.getDeadline()))
                            .createdAt(dto.getCreatedAt())
                            .status(dto.getStatus())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageResponse<>(sales.getTotalElements(), sales.getNumber() + 1, content);
    }

    // 내가 쓴 후기 조회
    public PageResponse<ReviewHistoryResponse> getReviewsByMe(Long userId, String type, String reviewSort, boolean writtenStatus, int page, int size) {

        Sort sortOption = getSort(reviewSort, type);
        Pageable pageable = PageRequest.of(page -1, size, sortOption);

        Page<Review> reviews = reviewRepository.findReviewsByMe(userId, type, writtenStatus, pageable);

        List<ReviewHistoryResponse> content = reviews.getContent().stream()
                .map(review -> {
                    ChatRoom chatRoom = review.getChatRoom();
                    Post post = chatRoom.getPost();

                    return ReviewHistoryResponse.builder()
                            .postId(post.getId())
                            .reviewId(review.getId())
                            .isWritten(review.getStar() != 0.0)
                            .title(post.getTitle())
                            .type(chatRoom.getSeller().getId().equals(userId) ? "sale" : "purchase")
                            .transactionDate(chatRoom.getFinishDate())
                            .choice(review.getChoice())
                            .content(review.getContent())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageResponse<>(reviews.getTotalElements(), reviews.getNumber() + 1, content);
    }

    // 받은 후기 조회
    public PageResponse<ReviewHistoryResponse> getReviewsByOther(Long myId, Long userId, boolean writtenStatus, String type, String reviewSort, String starSort, int page, int size) {

        Sort sortOption = getReviewSort(starSort, reviewSort);
        Pageable pageable = PageRequest.of(page - 1, size, sortOption);

        Page<Review> reviews = reviewRepository.findReviewsByOther(userId, writtenStatus, type, pageable);

        List<ReviewHistoryResponse> content = reviews.getContent().stream()
                .map(review -> {
                    ChatRoom chatRoom = review.getChatRoom();
                    Post post = chatRoom.getPost();

                    return ReviewHistoryResponse.builder()
                            .postId(post.getId())
                            .reviewId(review.getId())
                            .isWritten(review.getStar() != 0.0)
                            .title(post.getTitle())
                            .type(chatRoom.getSeller().getId().equals(userId) ? "sale" : "purchase")
                            .transactionDate(chatRoom.getFinishDate())
                            .choice(review.getChoice())
                            .content(review.getContent())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageResponse<>(reviews.getTotalElements(), reviews.getNumber() + 1, content);
    }

    // 찜 내역 조회
    public PageResponse<PostLikeHistoryResponse> getWishlist(Long userId, String postLikeSort, PostStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size, getWishlistSort(postLikeSort));

        Page<PostLikeProjectionDto> likes = postLikeRepository.findPostLikeWithRegion(userId, status, pageable);

        List<PostLikeHistoryResponse> content = likes.getContent().stream()
            .map(dto -> {
                String thumbnail = postImageRepository.findThumbnailUrlByPostId(dto.getPostId())
                        .orElse(null);

                return PostLikeHistoryResponse.builder()
                        .postId(dto.getPostId())
                        .title(dto.getTitle())
                        .price(dto.getPrice())
                        .thumbnail(thumbnail)
                        .region(dto.getRegion())
                        .createdAt(dto.getCreatedAt())
                        //마감일 지나면 날짜가 음수로 이상하게 출력될 수 있을 것 같음. 추후 업데이트 고려하기.
                        .daysLeft((int) ChronoUnit.DAYS.between(LocalDateTime.now(), dto.getDeadline()))
                        .status(dto.getStatus())
                        .build();
            }).collect(Collectors.toList());

        return new PageResponse<>(likes.getTotalElements(), likes.getNumber() + 1, content);
    }

    // 리뷰 등록
    @Transactional
    public void registerReview(Long userId, Long reviewId, ReviewRegisterRequest request) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 리뷰가 존재하지 않습니다."));

        if (!review.getWriter().getId().equals(userId) &&
                !review.getTarget().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "리뷰 작성 권한이 없습니다.");
        }

        boolean hasChoice = request.getChoice() != null;
        boolean hasContent = request.getContent() != null;

        review.setStar(request.getStar());

        // NOT choice && content 검증로직
        if(hasChoice) {
            review.setChoice(request.getChoice());
            review.setContent(null);
        } else if(hasContent) {
            review.setContent(request.getContent());
            review.setChoice(null);
        } else {
            review.setChoice(null);
            review.setContent(null);
        }
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 리뷰가 존재하지 않습니다."));

        // 실제 삭제가아닌 Review DTO에서 isWritten 판단 기준인 star(별점) 기준 및 choice, content 초기값으로 세팅
        // soft-delete 방식 사용
        review.setStar(0.0);
        review.setChoice(null);
        review.setContent(null);
    }

}