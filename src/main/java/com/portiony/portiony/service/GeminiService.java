package com.portiony.portiony.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portiony.portiony.dto.PostCardDto;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.entity.UserPreference;
import com.portiony.portiony.repository.PostImageRepository;
import com.portiony.portiony.repository.PostRepository;
import com.portiony.portiony.util.UserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    @Value("${gemini.api-key}")
    private String apiKey;

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    public List<PostCardDto> recommendPostCards(UserPreference pref) throws Exception {
        List<Post> allPosts = postRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc();
        String prompt = buildPrompt(pref, allPosts);
        String response = callGemini(prompt);
        return parseRecommended(response, allPosts);
    }

    private String buildPrompt(UserPreference pref, List<Post> posts) {
        StringBuilder sb = new StringBuilder();

        sb.append("아래 조건에 어울리는 공동구매 게시물 중에서 추천할 게시글 12개의 번호만 추려줘. ")
                .append("결과는 번호만 쉼표로 구분해서 줘. 다른 말은 절대 하지 마. 예: 1, 3, 7\n\n");

        sb.append("조건:\n");
        if (pref.getPurchaseReason() != 0) {
            sb.append("- 구매 이유: ").append(UserPreferenceMapper.getPurchaseReason(pref.getPurchaseReason())).append("\n");
        }
        if (pref.getSituation() != 0) {
            sb.append("- 상황: ").append(UserPreferenceMapper.getSituation(pref.getSituation())).append("\n");
        }
        if (pref.getMainCategory() != 0) {
            sb.append("- 관심 카테고리: ").append(UserPreferenceMapper.getCategoryName(pref.getMainCategory())).append("\n");
        }

        sb.append("\n아래는 추천 대상 게시글 목록이야:\n");
        int index = 1;
        for (Post post : posts) {
            sb.append(index++)
                    .append(". ")
                    .append(post.getTitle())
                    .append(" - ")
                    .append(post.getDeadline() != null ? post.getDeadline().toLocalDate() : "마감일 없음")
                    .append(" - ")
                    .append(post.getStatus().toKorean())
                    .append("\n");
        }

        return sb.toString();
    }

    private String callGemini(String prompt) throws Exception {
        OkHttpClient client = new OkHttpClient();

        String json = """
        {
          "contents": [
            {
              "parts": [
                { "text": "%s" }
              ]
            }
          ]
        }
        """.formatted(prompt);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        HttpUrl url = HttpUrl.parse(API_URL).newBuilder()
                .addQueryParameter("key", apiKey)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Gemini API 호출 실패: " + response.code());
            }
            String responseBody = response.body().string();
            System.out.println("Gemini 응답: " + responseBody);
            return responseBody;
        }
    }

    private List<PostCardDto> parseRecommended(String responseBody, List<Post> posts) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);

        List<PostCardDto> result = new ArrayList<>();

        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && parts.size() > 0) {
                String text = parts.get(0).path("text").asText();
                String[] numbers = text.replaceAll("[^0-9,]", "").split(",");

                for (String numStr : numbers) {
                    try {
                        int idx = Integer.parseInt(numStr.trim()) - 1;
                        if (idx >= 0 && idx < posts.size()) {
                            Post post = posts.get(idx);
                            PostCardDto dto = PostCardDto.builder()
                                    .id(post.getId())
                                    .title(post.getTitle())
                                    .price(post.getPrice())
                                    .unit(post.getUnit())
                                    .capacity(post.getCapacity())
                                    .completedCount(0)
                                    .status(post.getStatus().toKorean())
                                    .deadline(post.getDeadline().toLocalDate())
                                    .thumbnail(null) // 필요 시 이미지 URL 연결
                                    .build();
                            result.add(dto);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return result;
    }
}
