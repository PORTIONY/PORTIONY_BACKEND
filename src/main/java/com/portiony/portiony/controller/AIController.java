package com.portiony.portiony.controller;

import com.portiony.portiony.dto.PostCardDto;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.UserPreference;
import com.portiony.portiony.entity.enums.PostStatus;
import com.portiony.portiony.repository.ChatRoomRepository;
import com.portiony.portiony.repository.PostImageRepository;
import com.portiony.portiony.repository.PostRepository;
import com.portiony.portiony.repository.UserPreferenceRepository;
import com.portiony.portiony.security.CustomUserDetails;
import com.portiony.portiony.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class AIController {

    private final GeminiService geminiService;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @GetMapping("/recommend")
    public ResponseEntity<?> recommend(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) String subregionId,
            @RequestParam(required = false) String dongId,
            @RequestParam(required = false) String selectedCategory // 프론트에서 보내는 사람용 이름
    ) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof CustomUserDetails userDetails)) {
                return ResponseEntity.status(401).body("인증된 사용자 아닙니다.");
            }
            Long userId = userDetails.getUser().getId();

            Optional<UserPreference> optionalPref = userPreferenceRepository.findByUserId(userId);
            boolean hasValidPref = optionalPref.isPresent() &&
                    !(optionalPref.get().getMainCategory() == 0 &&
                            optionalPref.get().getPurchaseReason() == 0 &&
                            optionalPref.get().getSituation() == 0);

            PostStatus postStatus = null;
            if (status != null && !status.isBlank()) {
                try {
                    postStatus = PostStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("잘못된 상태값입니다: " + status);
                }
            }

            Long region = regionId != null ? Long.parseLong(regionId) : null;
            Long subregion = subregionId != null ? Long.parseLong(subregionId) : null;
            Long dong = dongId != null ? Long.parseLong(dongId) : null;

            String categoryCode = mapCategoryNameToCode(selectedCategory);

            boolean isFirstPage = page == 1;
            boolean applyAI = isFirstPage && hasValidPref;

            List<PostCardDto> aiRecommended = new ArrayList<>();
            if (applyAI) {
                try {
                    aiRecommended = geminiService.recommendPostCards(optionalPref.get());
                } catch (Exception e) {
                    System.out.println("Gemini 추천 실패: " + e.getMessage());
                }
            }

            if (isFirstPage && !aiRecommended.isEmpty()) {
                int aiCount = aiRecommended.size();
                int remainingSize = size - aiCount;

                Pageable remainingPageable = PageRequest.of(0, Math.max(0, remainingSize), getSort(sort));
                Page<Post> generalPostPage = postRepository.findFilteredPosts(
                        postStatus, keyword, region, subregion, dong, categoryCode, remainingPageable
                );

                long generalTotal = postRepository.findFilteredPosts(
                        postStatus, keyword, region, subregion, dong, categoryCode,
                        PageRequest.of(0, 1)
                ).getTotalElements();

                long total = generalTotal + aiCount;

                Map<Long, Long> completedCountMap = chatRoomRepository.countCompletedByPostIdGrouped().stream()
                        .collect(Collectors.toMap(
                                obj -> (Long) obj[0],
                                obj -> (Long) obj[1]
                        ));

                List<PostCardDto> generalPosts = generalPostPage.getContent().stream()
                        .map(post -> PostCardDto.builder()
                                .id(post.getId())
                                .title(post.getTitle())
                                .price(post.getPrice())
                                .unit(post.getUnit())
                                .capacity(post.getCapacity())
                                .completedCount(completedCountMap.getOrDefault(post.getId(), 0L).intValue())
                                .status(post.getStatus().toKorean())
                                .deadline(post.getDeadline().toLocalDate())
                                .thumbnail(getThumbnail(post.getId()))
                                .build())
                        .toList();

                List<PostCardDto> combined = new ArrayList<>();
                combined.addAll(aiRecommended);
                combined.addAll(generalPosts);

                Map<String, Object> response = new HashMap<>();
                response.put("total", total);
                response.put("page", page);
                response.put("posts", combined);
                response.put("isAI", true);
                return ResponseEntity.ok(response);
            }

            Pageable pageable = PageRequest.of(page - 1, size, getSort(sort));
            Page<Post> postPage = postRepository.findFilteredPosts(
                    postStatus, keyword, region, subregion, dong, categoryCode, pageable
            );
            long filteredTotal = postPage.getTotalElements();

            Map<Long, Long> completedCountMap = chatRoomRepository.countCompletedByPostIdGrouped().stream()
                    .collect(Collectors.toMap(
                            obj -> (Long) obj[0],
                            obj -> (Long) obj[1]
                    ));

            List<PostCardDto> posts = postPage.getContent().stream()
                    .map(post -> PostCardDto.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .price(post.getPrice())
                            .unit(post.getUnit())
                            .capacity(post.getCapacity())
                            .completedCount(completedCountMap.getOrDefault(post.getId(), 0L).intValue())
                            .status(post.getStatus().toKorean())
                            .deadline(post.getDeadline().toLocalDate())
                            .thumbnail(getThumbnail(post.getId()))
                            .build())
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("total", filteredTotal);
            response.put("page", page);
            response.put("posts", posts);
            response.put("isAI", false);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("추천 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private String mapCategoryNameToCode(String name) {
        if (name == null || name.equals("전체") || name.isBlank()) {
            return null;
        }

        switch (name) {
            case "생활용품":
                return "C001";
            case "반려동물":
                return "C002";
            case "의류":
                return "C003";
            case "문구류":
                return "C004";
            case "육아용품":
                return "C005";
            case "화장품/뷰티":
                return "C006";
            case "잡화/기타":
                return "C007";
            default:
                return null;
        }
    }


    private Sort getSort(String sortType) {
        if ("oldest".equalsIgnoreCase(sortType)) {
            return Sort.by(Sort.Direction.ASC, "createdAt");
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    private String getThumbnail(Long postId) {
        var image = postImageRepository.findFirstImageUrlByPostId(postId);
        return image != null ? image.getImageUrl() : null;
    }
}
