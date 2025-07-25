package com.portiony.portiony.service;

import com.portiony.portiony.dto.*;
import com.portiony.portiony.dto.common.PageResponse;
import com.portiony.portiony.dto.user.*;
import com.portiony.portiony.entity.*;
import com.portiony.portiony.entity.enums.PostStatus;
import com.portiony.portiony.entity.enums.UserStatus;
import com.portiony.portiony.repository.*;
import com.portiony.portiony.security.CustomUserDetails;
import com.portiony.portiony.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.multipart.MultipartFile;
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
@Slf4j
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
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());

        if (existing.isPresent()) {
            User user = existing.get();

            if (user.getStatus() == UserStatus.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
            }

            if (user.getStatus() == UserStatus.WITHDRAWN) {
                LocalDateTime withdrawnAt = user.getUpdatedAt();
                if (withdrawnAt != null && withdrawnAt.isAfter(LocalDateTime.now().minusMonths(1))) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "탈퇴 후 1개월 동안 재가입할 수 없습니다.");
                } else {
                    // 1개월 지난 계정은 삭제처리
                    userRepository.delete(user);
                }
            }
        }

        User user = createUser(dto);
        saveAgreementsAndPreferences(dto, user);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

        return new LoginResponseDto(accessToken, refreshToken, user.getId());
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "탈퇴 계정입니다.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();

        return new LoginResponseDto(accessToken, refreshToken, user.getId());
    }

    public Object kakaoLogin(String code) {

        // 1. access token 요청
        String tokenUri = "https://kauth.kakao.com/oauth/token";

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", "http://localhost:3000/login/oauth/kakao"); // 프론트에서 인가코드 받은 주소
        params.add("code", code); // 프론트에서 받은 인가 코드

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<LinkedMultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, tokenHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.postForEntity(
                tokenUri, tokenRequest, (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "카카오 토큰 요청 실패");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. 사용자 정보 요청
        String userInfoUri = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);
        ResponseEntity<Map<String, Object>> userInfoResponse = restTemplate.exchange(
                userInfoUri, HttpMethod.GET, userInfoRequest, (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "카카오 사용자 정보 요청 실패");
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoResponse.getBody().get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "카카오 계정에 이메일 정보가 없습니다.");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            // 기존 사용자: 토큰 발급 후 로그인 완료
            User user = userOptional.get();
            String jwtAccessToken = jwtUtil.generateAccessToken(user.getEmail());
            String jwtRefreshToken = refreshTokenService.createRefreshToken(user.getEmail()).getToken();
            return new LoginResponseDto(jwtAccessToken, jwtRefreshToken, user.getId());
        } else {
            // 신규 사용자: 회원가입 요청용 정보 반환
            String nickname = (String) profile.get("nickname");
            String profileImage = (String) profile.get("profile_image_url");

            return new KakaoSignupRequestDto(
                    email,
                    nickname,
                    profileImage,
                    null, // regionId
                    null, // subregionId
                    null, // dongId
                    null, // agreementIds
                    null, // mainCategory
                    null, // purchaseReason
                    null  // situation
            );
        }
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

        return new KakaoSignupResponseDto(accessToken, refreshToken, user.getId());
    }

    // 이메일 중복 확인
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 닉네임 중복 확인
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
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
            result = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if ("oldest".equalsIgnoreCase(reviewSort)) {
            result = Sort.by(Sort.Direction.ASC, "createdAt");
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
    private Sort getWishlistSort(String postLikeSort) {
        switch (postLikeSort) {
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
    public UserProfileResponse getUserProfile(CustomUserDetails userDetails) {

        User user = userDetails.getUser();

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
    public EditProfileViewResponse editProfileView(CustomUserDetails userDetails) {
        User  user = userDetails.getUser();

        return EditProfileViewResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .build();
    }

    // 프로필 편집
    //s3 서비스 구현 시 파라미터에 프로필이미지 추가하기
    @Transactional
    public void editProfile(CustomUserDetails userDetails, EditProfileRequest request) {

        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));


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

            log.info("[DEBUG] 수정 전 DB 비밀번호: {}", user.getPassword());
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.saveAndFlush(user);
            log.info("[DEBUG] 수정 후 DB 비밀번호: {}", userRepository.findById(user.getId()).get().getPassword());
        }

        // 프로필 이미지 수정 (s3Service 구현 시 추후 주석 해제)
//        if (profileImage != null && !profileImage.isEmpty()) {
//            String imageUrl = s3Service.upload(profileImage, "profile-images");
//            user.setProfileImage(imageUrl);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(CustomUserDetails userDetails, DeleteUserRequest request) {

        User  user = userDetails.getUser();

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"비밀번호가 일치하지 않습니다.");
        }

        user.setStatus(UserStatus.WITHDRAWN);
        userRepository.saveAndFlush(user);
        log.info("[DEBUG] 현재회원상태: {}", user.getStatus());
    }

    // 거래 완료 인원 계산(성능 이슈로 추후 디벨롭 때에 쿼리 변경 고민해보기)
    private Map<Long, Integer> getCompletedCountMap() {
        return chatRoomRepository.countCompletedByPostIdGrouped().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    // 썸네일url 추출
    private String getThumbnailUrl(Long postId) {
        return postImageRepository.findThumbnailUrlByPostId(postId)
                .orElse(null);
    }

    // 디테일필드 출력
    private String buildDetails(int capacity, int completed) {
        if (capacity == 0) return "정보 없음";
        return "공구인원 " + capacity + "명 · 거래완료 " + completed + "명";
    }

    // 마감일출력 포맷
    private String formatDaysLeft(LocalDateTime deadline) {
        long diff = ChronoUnit.DAYS.between(LocalDateTime.now(), deadline);
        return diff < 0 ? "공구 마감" : "D-" + diff;
    }

    // 내 구매 내역 조회
    public PageResponse<PurchaseHistoryResponse> getMyPurchases(CustomUserDetails userDetails, String dateSort, String priceSort, int page, int size) {

        User  user = userDetails.getUser();

        Pageable pageable = PageRequest.of(page - 1, size, getSort(dateSort, priceSort));

        Page<PurchaseProjectionDto> purchases = chatRoomRepository.findPurchasesWithPost(user.getId(), pageable);

        List<PurchaseHistoryResponse> content = purchases.getContent().stream()
                .map(dto -> {
                    String daysLeft = formatDaysLeft(dto.getDeadline());
                    String thumbnail = getThumbnailUrl(dto.getPostId());
                    Map<Long, Integer> completedCountMap = getCompletedCountMap();
                    String details = buildDetails(dto.getCapacity(), completedCountMap.getOrDefault(dto.getPostId(), 0));

                    return PurchaseHistoryResponse.builder()
                            .postId(dto.getPostId())
                            .title(dto.getTitle())
                            .price(dto.getPrice())
                            .thumbnail(thumbnail)
                            .region(dto.getRegion())
                            .purchasedAt(dto.getCreatedAt())
                            .daysLeft((daysLeft))
                            .details(details)
                            .build();
                })
                .collect(Collectors.toList());

        return new PageResponse<>(purchases.getTotalElements(), purchases.getNumber() + 1, content);
    }

    // 판매 내역 조회 (특정 유저)
    public PageResponse<SaleHistoryResponse> getMySales(CustomUserDetails userDetails, Long userId, String dateSort, String priceSort, PostStatus status, int page, int size) {
        User user = userDetails.getUser();

        Pageable pageable = PageRequest.of(page - 1, size, getSort(dateSort, priceSort));

        Page<SaleProjectionDto> sales = chatRoomRepository.findSalesWithPost(user.getId(), status, pageable);

        List<SaleHistoryResponse> content = sales.getContent().stream()
                .map(dto -> {
                    String daysLeft = formatDaysLeft(dto.getDeadline());
                    String thumbnail = getThumbnailUrl(dto.getPostId());
                    Map<Long, Integer> completedCountMap = getCompletedCountMap();
                    String details = buildDetails(dto.getCapacity(), completedCountMap.getOrDefault(dto.getPostId(), 0));

                    return SaleHistoryResponse.builder()
                            .postId(dto.getPostId())
                            .title(dto.getTitle())
                            .price(dto.getPrice())
                            .thumbnail(thumbnail)
                            .region(dto.getRegion())
                            .createdAt(dto.getCreatedAt())
                            .daysLeft((daysLeft))
                            .status(dto.getStatus())
                            .details(details)
                            .build();
                })
                .collect(Collectors.toList());

        return new PageResponse<>(sales.getTotalElements(), sales.getNumber() + 1, content);
    }

    // 찜 내역 조회
    public PageResponse<PostLikeHistoryResponse> getWishlist(CustomUserDetails userDetails, String postLikeSort, PostStatus status, int page, int size) {

        User user = userDetails.getUser();

        Pageable pageable = PageRequest.of(page - 1, size, getWishlistSort(postLikeSort));

        Page<PostLikeProjectionDto> likes = postLikeRepository.findPostLikeWithRegion(user.getId(), status, pageable);

        List<PostLikeHistoryResponse> content = likes.getContent().stream()
                .map(dto -> {
                    String daysLeft = formatDaysLeft(dto.getDeadline());
                    String thumbnail = getThumbnailUrl(dto.getPostId());
                    Map<Long, Integer> completedCountMap = getCompletedCountMap();
                    String details = buildDetails(dto.getCapacity(), completedCountMap.getOrDefault(dto.getPostId(), 0));

                    return PostLikeHistoryResponse.builder()
                            .postId(dto.getPostId())
                            .title(dto.getTitle())
                            .price(dto.getPrice())
                            .thumbnail(thumbnail)
                            .region(dto.getRegion())
                            .createdAt(dto.getCreatedAt())
                            .daysLeft((daysLeft))
                            .status(dto.getStatus())
                            .details(details)
                            .build();
                }).collect(Collectors.toList());

        return new PageResponse<>(likes.getTotalElements(), likes.getNumber() + 1, content);
    }

    // 구매*판매 필터링
    private String getReviewType(ChatRoom chatRoom, Long userId) {
        if (chatRoom.getBuyer() != null && chatRoom.getBuyer().getId().equals(userId)) {
            return "purchase";
        } else if (chatRoom.getSeller() != null && chatRoom.getSeller().getId().equals(userId)) {
            return "sale";
        } else {
            return "unknown"; // 예외
        }
    }

    // 내가 쓴 후기 조회 (내 프로필 진입)
    public PageResponse<ReviewHistoryResponse> getReviewsByMe(CustomUserDetails userDetails, String type, String reviewSort, Boolean writtenStatus, int page, int size) {

        User user = userDetails.getUser();

        Sort sortOption = getReviewSort(reviewSort, type);
        Pageable pageable = PageRequest.of(page -1, size, sortOption);

        Page<Review> allReviews = reviewRepository.findAllReviewsByMe(user.getId(), pageable);

        List<ReviewHistoryResponse> content = allReviews.getContent().stream()
                .filter(review -> {
                    // 후기 작성 여부 필터
                    if (writtenStatus != null) {
                        if (writtenStatus && review.getStar() == 0.0) return false;
                        if (!writtenStatus && review.getStar() != 0.0) return false;
                    }

                    // 구매/판매 타입 필터
                    ChatRoom cr = review.getChatRoom();
                    if (type != null) {
                        if (type.equals("buyer") && !cr.getBuyer().getId().equals(user.getId())) return false;
                        if (type.equals("seller") && !cr.getSeller().getId().equals(user.getId())) return false;
                    }

                    return true;
                })

                .map(review -> {
                    ChatRoom chatRoom = review.getChatRoom();
                    Post post = chatRoom.getPost();

                    String reviewType = getReviewType(chatRoom, user.getId());

                    return ReviewHistoryResponse.builder()
                            .postId(post.getId())
                            .reviewId(review.getId())
                            .isWritten(review.getStar() != 0.0)
                            .title(post.getTitle())
                            .type(reviewType)
                            .transactionDate(chatRoom.getFinishDate())
                            .choice(review.getChoice())
                            .content(review.getContent())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageResponse<>(allReviews.getTotalElements(), allReviews.getNumber() + 1, content);
    }

    // 받은 후기 조회 (타유저 프로필 진입)
    public PageResponse<ReviewHistoryResponse> getReviewsByOther(CustomUserDetails userDetails, Long userId, String type, String reviewSort, String starSort, int page, int size) {

        User user = userDetails.getUser();

        Sort sortOption = getReviewSort(starSort, reviewSort);
        Pageable pageable = PageRequest.of(page - 1, size, sortOption);

        Page<Review> reviews = reviewRepository.findReviewsByOther(user.getId(), type, pageable);

        List<ReviewHistoryResponse> content = reviews.getContent().stream()
                .map(review -> {
                    ChatRoom chatRoom = review.getChatRoom();
                    Post post = chatRoom.getPost();

                    String reviewType = getReviewType(chatRoom, user.getId());

                    return ReviewHistoryResponse.builder()
                            .postId(post.getId())
                            .reviewId(review.getId())
                            .isWritten(review.getStar() != 0.0)
                            .title(post.getTitle())
                            .type(reviewType)
                            .transactionDate(chatRoom.getFinishDate())
                            .choice(review.getChoice())
                            .content(review.getContent())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageResponse<>(reviews.getTotalElements(), reviews.getNumber() + 1, content);
    }

    // 리뷰 등록
    @Transactional
    public void registerReview(CustomUserDetails userDetails, Long reviewId, ReviewRegisterRequest request) {

        User user = userDetails.getUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 리뷰가 존재하지 않습니다."));

        if (!review.getWriter().getId().equals(user.getId()) &&
                !review.getTarget().getId().equals(user.getId())) {
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
    // 사용자에게 삭제된 것 처럼 보이지만 star, choice, content null 처리 (soft-delete 방식으로 처리)
    @Transactional
    public void deleteReview(CustomUserDetails userDetails, Long reviewId) {

        User user = userDetails.getUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 리뷰가 존재하지 않습니다."));

        // 실제 삭제가아닌 Review DTO에서 isWritten 판단 기준인 star(별점) 기준 및 choice, content 초기값으로 세팅
        // soft-delete 방식 사용
        review.setStar(0.0);
        review.setChoice(null);
        review.setContent(null);
    }

}