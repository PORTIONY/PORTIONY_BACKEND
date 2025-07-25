package com.portiony.portiony.controller;

import com.portiony.portiony.dto.PostCardDto;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.UserPreference;
import com.portiony.portiony.entity.enums.ChatStatus;
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

import java.time.LocalDate;
import java.util.*;

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
    public ResponseEntity<?> recommend(@RequestParam(defaultValue = "1") int page) {
        try {
            // 🔐 인증된 사용자 가져오기
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof CustomUserDetails userDetails)) {
                return ResponseEntity.status(401).body("인증된 사용자가 아닙니다.");
            }

            Long userId = userDetails.getUser().getId();
            Optional<UserPreference> optionalPref = userPreferenceRepository.findByUserId(userId);

            boolean hasValidPreference = optionalPref.isPresent() &&
                    !(optionalPref.get().getMainCategory() == 0 &&
                            optionalPref.get().getPurchaseReason() == 0 &&
                            optionalPref.get().getSituation() == 0);

            List<Post> resultPosts;

            if (page == 1 && hasValidPreference) {
                UserPreference pref = optionalPref.get();
                String reason = UserPreferenceMapper.getPurchaseReason(pref.getPurchaseReason());
                String situation = UserPreferenceMapper.getSituation(pref.getSituation());
                Long categoryId = pref.getMainCategory().longValue();

                List<Post> filtered = postRepository.findAllByIsDeletedFalseAndCategory_Id(categoryId);
                resultPosts = geminiService.generateRecommendation(filtered, reason, situation);
            } else {
                // 📃 일반 전체 목록
                int adjustedPage = (hasValidPreference && page >= 2) ? page - 1 : page;
                Pageable pageable = PageRequest.of(adjustedPage - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Post> postPage = postRepository.findRecentPosts(pageable);
                resultPosts = postPage.getContent();
            }


            List<Object[]> completedCountsRaw = chatRoomRepository.countCompletedByPostIdGrouped();
            Map<Long, Integer> completedCountMap = new HashMap<>();
            for (Object[] row : completedCountsRaw) {
                Long postId = (Long) row[0];
                Long count = (Long) row[1];
                completedCountMap.put(postId, count.intValue());
            }

            List<PostCardDto> cards = resultPosts.stream()
                    .map(post -> {
                        int completedCount = completedCountMap.getOrDefault(post.getId(), 0);

                        return PostCardDto.builder()
                                .id(post.getId())
                                .title(post.getTitle())
                                .price(post.getPrice())
                                .unit(post.getUnit())
                                .capacity(post.getCapacity())
                                .completedCount(completedCount)
                                .status(post.getStatus().toKorean()) // 예: "PROGRESS" → "공구 중"
                                .deadline(post.getDeadline().toLocalDate())
                                .thumbnail(getThumbnail(post.getId()))
                                .build();
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("total", cards.size());
            response.put("page", page);
            response.put("posts", cards);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("추천 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private String getThumbnail(Long postId) {
        var image = postImageRepository.findFirstImageUrlByPostId(postId);
        return image != null ? image.getImageUrl() : null;
    }
}
