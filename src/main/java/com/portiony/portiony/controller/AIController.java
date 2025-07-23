package com.portiony.portiony.controller;

import com.portiony.portiony.dto.PostDto;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.UserPreference;
import com.portiony.portiony.repository.PostRepository;
import com.portiony.portiony.repository.UserPreferenceRepository;
import com.portiony.portiony.service.GeminiService;
import com.portiony.portiony.util.UserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class AIController {

    private final GeminiService geminiService;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PostRepository postRepository;

    /**
     * 선호 정보가 없거나(존재하지 않거나), 전부 0이면 최신순 게시글
     * 아니면 Gemini 추천
     * GET /api/post/recommend/{userId}?page=1
     */
    @GetMapping("/recommend/{userId}")
    public ResponseEntity<?> recommend(@PathVariable Long userId,
                                       @RequestParam(defaultValue = "1") int page) {
        try {
            Optional<UserPreference> optionalPref = userPreferenceRepository.findByUserId(userId);

            // [1] 선호 정보가 없거나, 전부 0이면 → 최신순 게시글 반환
            if (optionalPref.isEmpty() ||
                    (optionalPref.get().getMainCategory() == 0 &&
                            optionalPref.get().getPurchaseReason() == 0 &&
                            optionalPref.get().getSituation() == 0)) {

                Pageable pageable = PageRequest.of(page - 1, 10); // 0-based
                Page<Post> postPage = postRepository.findRecentPosts(pageable);

                List<PostDto> dtoList = postPage.getContent().stream()
                        .map(PostDto::from)
                        .toList();

                return ResponseEntity.ok(dtoList);
            }

            // [2] 선호 정보가 있는 경우 → Gemini 추천 로직 실행
            UserPreference pref = optionalPref.get();

            String reason = UserPreferenceMapper.getPurchaseReason(pref.getPurchaseReason());
            String situation = UserPreferenceMapper.getSituation(pref.getSituation());
            Long categoryId = pref.getMainCategory().longValue();

            List<Post> filteredPosts = postRepository.findAllByIsDeletedFalseAndCategory_Id(categoryId);
            if (filteredPosts.isEmpty()) {
                return ResponseEntity.ok("추천할 게시글이 없습니다.");
            }

            List<Post> recommendedPosts = geminiService.generateRecommendation(filteredPosts, reason, situation);
            List<PostDto> dtoList = recommendedPosts.stream()
                    .map(PostDto::from)
                    .toList();

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("추천 실패: " + e.getMessage());
        }
    }
}
