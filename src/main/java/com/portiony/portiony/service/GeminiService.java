package com.portiony.portiony.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portiony.portiony.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    @Value("${gemini.api-key}")
    private String apiKey;

    public List<Post> generateRecommendation(List<Post> postList, String reason, String situation) throws Exception {
        String prompt = buildPrompt(postList, reason, situation);
        String response = callGemini(prompt);
        return parseRecommendedPosts(response, postList);
    }

    private String buildPrompt(List<Post> posts, String reason, String situation) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("‘%s’, ‘%s’인 사용자에게 어울리는 상품 10개를 아래 리스트에서 골라줘.\n", reason, situation));
        sb.append("상품 리스트:\n");

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            sb.append(String.format("%d. %s (%s) - %s\n",
                    i + 1,
                    p.getTitle(),
                    p.getCategory().getTitle(),
                    p.getDescription()
            ));
        }

        sb.append("추천 결과는 번호만 콤마로 구분해서 알려줘. 예: 1, 3, 5, 6, 10");
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
            return response.body().string();
        }
    }

    private List<Post> parseRecommendedPosts(String responseBody, List<Post> allPosts) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);

        List<Post> result = new ArrayList<>();
        Set<Integer> addedIndices = new HashSet<>();

        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && parts.size() > 0) {
                String text = parts.get(0).path("text").asText();
                String cleaned = text.replaceAll("[^0-9,]", "").replaceAll(",{2,}", ",");
                String[] nums = cleaned.split(",");

                for (String numStr : nums) {
                    try {
                        int idx = Integer.parseInt(numStr.trim()) - 1;
                        if (idx >= 0 && idx < allPosts.size() && addedIndices.add(idx)) {
                            result.add(allPosts.get(idx));
                            if (result.size() == 10) break;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return result;
    }
}
