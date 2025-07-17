package com.portiony.portiony.service;

import com.portiony.portiony.dto.*;
import com.portiony.portiony.dto.common.PageResponse;
import com.portiony.portiony.dto.user.*;
import com.portiony.portiony.entity.*;
import com.portiony.portiony.entity.enums.PostStatus;
import com.portiony.portiony.entity.enums.UserStatus;
import com.portiony.portiony.repository.*;
import com.portiony.portiony.specification.ChatRoomSpecifications;
import com.portiony.portiony.specification.PostLikeSpecifications;
import com.portiony.portiony.specification.ReviewSpecifications;
import com.portiony.portiony.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final ChatRoomRepository chatRoomRepository;
    private final PostImageRepository postImageRepository;
    private final ReviewRepository reviewRepository;
    private final PostLikeRepository postLikeRepository;
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

    private User findUserById(Long userId) {
        return userRepository.findById(userId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"사용자를 찾을 수 없습니다."));
    }

    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserById(userId);
        // TODO: userId 기반 유저 조회
        // TODO: 구매이력, 판매이력 수 계산 (주문/게시글 도메인 연동)
        // TODO: 긍정후기비율 계산 (리뷰 도메인 연동)

        return  new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getPurchase_count(),
                user.getSalesCount(),
                user.getStar(),
                true
        );
    }

    public EditProfileViewResponse editProfileView(Long userId) {
        User user = findUserById(userId);

        return EditProfileViewResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .build();
    }

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

    @Transactional
    public void deleteUser(Long userId, DeleteUserRequest request) {
        User user = findUserById(userId);

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"비밀번호가 일치하지 않습니다.");
        }

        user.setStatus(UserStatus.WITHDRAWN);
    }

    private Sort getSort(String sort, String priceOrder) {
        Sort result = Sort.by(Sort.Direction.DESC, "post.createdAt");

        if ("recent".equals(sort)) {
            result = Sort.by(Sort.Direction.DESC, "post.createdAt");
        } else if ("oldest".equals(sort)) {
            result = Sort.by(Sort.Direction.ASC, "post.createdAt");
        }

        if ("asc".equals(priceOrder)) {
            result = result.and(Sort.by(Sort.Direction.ASC, "post.price"));
        } else if ("desc".equals(priceOrder)) {
            result = result.and(Sort.by(Sort.Direction.DESC, "post.price"));
        }

        return result;
    }

    public PageResponse<PurchaseHistoryResponse> getMyPurchases(Long userId, String sort, String priceOrder, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, getSort(sort, priceOrder));

        Page<PurchaseProjectionDto> purchases = chatRoomRepository.findPurchasesIncludePost(userId, pageable);

        List<PurchaseHistoryResponse> content = purchases.getContent().stream()
                .map(dto -> {
                    String thumbnail = postImageRepository.findThumbnailUrlByPostId(dto.getPostId())
                            .get();

                    return new PurchaseHistoryResponse(
                            dto.getPostId(),
                            dto.getTitle(),
                            dto.getPrice(),
                            thumbnail,
                            dto.getRegion(),
                            (int) ChronoUnit.DAYS.between(LocalDateTime.now(), dto.getDeadline()),
                            dto.getPurchasedAt(),
                            dto.getStatus().name()
                    );
                })
                .collect(Collectors.toList());

        return new PageResponse<>(purchases.getTotalElements(), purchases.getNumber() + 1, content);
    }

    public PageResponse<SaleHistoryResponse> getMySales(Long myId, Long userId, String sort, String priceOrder, String statusFilter, int page, int size) {

        if (!myId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(page - 1, size, getSort(sort, priceOrder));

        Specification<ChatRoom> specification = ChatRoomSpecifications.sellerIdEquals(userId);

        if(statusFilter != null && !statusFilter.isEmpty()) {
            try{
                PostStatus status = PostStatus.valueOf(statusFilter.toUpperCase());
                specification = specification.and(ChatRoomSpecifications.postStatusEquals(status));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        Page<ChatRoom> sales =  chatRoomRepository.findAll(specification, pageable);

        List<SaleHistoryResponse> content = sales.getContent().stream()
                .map(chatRoom -> {
                    Post post = chatRoom.getPost();
                    String thumbnail = postImageRepository.findThumbnailUrlByPostId(post.getId()).get();
                    return new SaleHistoryResponse(
                            post.getId(),
                            post.getTitle(),
                            post.getPrice(),
                            thumbnail,
                            post.getUser().getRegion().getCity(),
                            (int) ChronoUnit.DAYS.between(LocalDateTime.now(), post.getDeadline()),
                            post.getCreatedAt(),
                            post.getStatus().name()
                    );
                })
                .collect(Collectors.toList());

        return new PageResponse<>(sales.getTotalElements(), sales.getNumber() + 1, content);
    }

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

    public PageResponse<ReviewHistoryResponse> getReviewsByMe(Long userId, String type, String sort, String writtenStatus, int page, int size) {
        Sort sortOption = getSort(sort, type);
        Pageable pageable = PageRequest.of(page -1, size, sortOption);

        Specification<Review> specification = ReviewSpecifications.filterReviewsByMe(userId, type, writtenStatus);
        Page<Review> reviews = reviewRepository.findAll(specification, pageable);

        List<ReviewHistoryResponse> content = reviews.getContent().stream()
                .map(review -> {
                    ChatRoom chatRoom = review.getChatRoom();
                    Post post = chatRoom.getPost();

                    return new ReviewHistoryResponse(
                            post.getId(),
                            review.getId(),
                            review.getStar() != 0.0,
                            post.getTitle(),
                            chatRoom.getSeller().getId().equals(userId) ? "sale" : "purchase",
                            chatRoom.getFinishDate().toLocalDate(),
                            review.getChoice(),
                            review.getContent()
                    );
                })
                .collect(Collectors.toList());

        return new PageResponse<>(reviews.getTotalElements(), reviews.getNumber() + 1, content);
    }

    public PageResponse<ReviewHistoryResponse> getReviewsByOther(Long myId, Long userId, String type, String reviewSort, String starSort, int page, int size) {

        if (!myId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Sort sortOption = getReviewSort(starSort, reviewSort);
        Pageable pageable = PageRequest.of(page - 1, size, sortOption);

        Specification<Review> specification = ReviewSpecifications.filterReviewsByOther(userId, type);
        Page<Review> reviews = reviewRepository.findAll(specification, pageable);

        List<ReviewHistoryResponse> content = reviews.getContent().stream()
                .map(review -> {
                    ChatRoom chatRoom = review.getChatRoom();
                    Post post = chatRoom.getPost();

                    return new ReviewHistoryResponse(
                            post.getId(),
                            review.getId(),
                            review.getStar() != 0.0,
                            post.getTitle(),
                            chatRoom.getSeller().getId().equals(userId) ? "sale" : "purchase",
                            chatRoom.getFinishDate().toLocalDate(),
                            review.getChoice(),
                            review.getContent()
                    );
                })
                .collect(Collectors.toList());

        return new PageResponse<>(reviews.getTotalElements(), reviews.getNumber() + 1, content);
    }

    private Sort getWishlistSort(String sort) {
        switch (sort) {
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

    public PageResponse<PostLikeResponse> getWishlist(Long userId, String sort, String status,int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, getWishlistSort(sort));
        Specification<PostLike> specification = PostLikeSpecifications.filterByStatusAndUser(userId, status);

        Page<PostLike> result = postLikeRepository.findAll(specification, pageable);

        List<PostLikeResponse> content = result.getContent().stream().map(dto -> {
            Post post = dto.getPost();
            String thumbnail = postImageRepository.findThumbnailUrlByPostId(post.getId())
                    .get();

            return new PostLikeResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getPrice(),
                    thumbnail,
                    post.getUser().getRegion().getCity(),
                    post.getCreatedAt(),
                    post.getDeadline(),
                    post.getStatus().name()
            );
        }).collect(Collectors.toList());

        return new PageResponse<>(result.getTotalElements(), result.getNumber() + 1, content);
    }

    @Transactional
    public void registerReview(Long userId, Long reviewId,ReviewRegisterRequest request) {
        Review review = reviewRepository.findById(reviewId).get();

        boolean hasChoice = request.getChoice() != null;
        boolean hasContent = request.getContent() != null;

        review.setStar(request.getStar());

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

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();

        review.setStar(0.0);
        review.setChoice(null);
        review.setContent(null);
    }

}