package com.portiony.portiony.controller;

import com.portiony.portiony.dto.PostDto;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.UserPreference;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class AIController {

    private final GeminiService geminiService;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PostRepository postRepository;

    @GetMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestParam(defaultValue = "1") int page) {
        try {

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

            int adjustedPage = (hasValidPreference && page >= 2) ? page - 1 : page;

            Pageable pageable = PageRequest.of(adjustedPage - 1, 10);
            Page<Post> postPage = postRepository.findRecentPosts(pageable);

            List<PostDto> dtoList = postPage.getContent().stream()
                    .map(PostDto::from)
                    .toList();

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("추천 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
