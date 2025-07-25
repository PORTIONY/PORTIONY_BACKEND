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
import com.portiony.portiony.util.UserPreferenceMapper;
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
            @RequestParam(required = false) String dongId
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

            if (page == 1 && hasValidPref) {
                try {
                    List<PostCardDto> recommended = geminiService.recommendPostCards(
                            UserPreferenceMapper.toGeminiPrompt(optionalPref.get())
                    );
                    if (recommended != null && !recommended.isEmpty()) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("total", recommended.size());
                        response.put("page", page);
                        response.put("posts", recommended);
                        return ResponseEntity.ok(response);
                    }
                } catch (Exception e) {
                    System.out.println("Gemini 호출 실패, 일반 목록으로 fallback: " + e.getMessage());
                }
            }

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

            Pageable pageable = PageRequest.of(page - 1, size, getSort(sort));
            Page<Post> postPage = postRepository.findFilteredPosts(postStatus, keyword, region, subregion, dong, pageable);

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
            response.put("total", postPage.getTotalElements());
            response.put("page", page);
            response.put("posts", posts);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("추천 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
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
