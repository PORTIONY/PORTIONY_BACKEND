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
     * 페이지 1일 때만 Gemini 추천 (선호 정보 존재 + 전부 0 아님)
     * 그 외에는 최신순 게시글 리턴
     */
    @GetMapping("/recommend/{userId}")
    public ResponseEntity<?> recommend(@PathVariable Long userId,
                                       @RequestParam(defaultValue = "1") int page) {
        try {
            Optional<UserPreference> optionalPref = userPreferenceRepository.findByUserId(userId);

            boolean hasValidPreference = optionalPref.isPresent() &&
                    !(optionalPref.get().getMainCategory() == 0 &&
                            optionalPref.get().getPurchaseReason() == 0 &&
                            optionalPref.get().getSituation() == 0);

            // ✅ page == 1 && 유효한 선호 정보 → Gemini 추천
            if (page == 1 && hasValidPreference) {
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
            }

            // ✅ 최신순 페이지 조정: 추천 본 사용자는 page=2부터 최신순 1페이지
            int adjustedPage = (hasValidPreference && page >= 2) ? page - 1 : page;

            Pageable pageable = PageRequest.of(adjustedPage - 1, 10);
            Page<Post> postPage = postRepository.findRecentPosts(pageable);

            List<PostDto> dtoList = postPage.getContent().stream()
                    .map(PostDto::from)
                    .toList();

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("추천 실패: " + e.getMessage());
        }
    }

}
