package com.portiony.portiony.controller;

import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.UserPreference;
import com.portiony.portiony.repository.PostRepository;
import com.portiony.portiony.repository.UserPreferenceRepository;
import com.portiony.portiony.service.GeminiService;
import com.portiony.portiony.util.UserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.portiony.portiony.dto.PostDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class AIController {

    private final GeminiService geminiService;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PostRepository postRepository;

    @GetMapping("/recommend/{userId}")
    public ResponseEntity<?> recommend(@PathVariable Long userId) {
        try {
            // 1. 사용자 선호 정보 가져오기
            UserPreference pref = userPreferenceRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 선호 정보가 없습니다."));

            String reason = UserPreferenceMapper.getPurchaseReason(pref.getPurchaseReason());
            String situation = UserPreferenceMapper.getSituation(pref.getSituation());
            String categoryTitle = UserPreferenceMapper.getCategoryName(pref.getMainCategory());

            // 2. 관심 카테고리 게시글만 필터링 (삭제되지 않은 것 중에서)
            List<Post> filteredPosts = postRepository.findAllByIsDeletedFalseAndCategory_Title(categoryTitle);

            if (filteredPosts.isEmpty()) {
                return ResponseEntity.ok("추천할 게시글이 없습니다.");
            }

            // 3. AI 추천 수행
            List<Post> recommendedPosts = geminiService.generateRecommendation(filteredPosts, reason, situation);

            // 4. 결과 반환
            List<PostDto> dtoList = recommendedPosts.stream()
                    .map(PostDto::from)
                    .toList();

            return ResponseEntity.ok(dtoList);


        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Gemini 추천 실패: " + e.getMessage());
        }
    }
}
