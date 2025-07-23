package com.portiony.portiony.controller;

import com.portiony.portiony.dto.PostDto;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.UserPreference;
import com.portiony.portiony.repository.PostRepository;
import com.portiony.portiony.repository.UserPreferenceRepository;
import com.portiony.portiony.service.GeminiService;
import com.portiony.portiony.util.UserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            // 1. 사용자 선호 정보 조회
            UserPreference pref = userPreferenceRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 선호 정보가 없습니다."));

            String reason = UserPreferenceMapper.getPurchaseReason(pref.getPurchaseReason());
            String situation = UserPreferenceMapper.getSituation(pref.getSituation());
            Long categoryId = pref.getMainCategory().longValue();

            // 2. 해당 카테고리의 게시글 조회 (삭제되지 않은 것만)
            List<Post> filteredPosts = postRepository.findAllByIsDeletedFalseAndCategory_Id(categoryId);
            if (filteredPosts.isEmpty()) {
                return ResponseEntity.ok("추천할 게시글이 없습니다.");
            }

            // 3. Gemini 추천
            List<Post> recommendedPosts = geminiService.generateRecommendation(filteredPosts, reason, situation);

            // 4. DTO 변환 및 반환
            List<PostDto> dtoList = recommendedPosts.stream()
                    .map(PostDto::from)
                    .toList();

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Gemini 추천 실패: " + e.getMessage());
        }
    }
}