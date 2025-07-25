package com.portiony.portiony.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portiony.portiony.dto.PostCardDto;
import com.portiony.portiony.entity.Post;
import com.portiony.portiony.repository.PostImageRepository;
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

    private final PostImageRepository postImageRepository;

    public List<PostCardDto> recommendPostCards(String prompt) throws Exception {
        String response = callGemini(prompt);
        return parseRecommended(response);
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

    private List<PostCardDto> parseRecommended(String responseBody) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);

        List<PostCardDto> result = new ArrayList<>();

        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray() && parts.size() > 0) {
                String text = parts.get(0).path("text").asText();
                // 예시 형식: 1. 복숭아 공동구매 - 7월 31일 마감 - 공구 중
                String[] lines = text.split("\\n");
                for (String line : lines) {
                    String[] partsLine = line.split(" - ");
                    if (partsLine.length < 3) continue;

                    String titlePart = partsLine[0].replaceAll("^\\d+\\.\\s*", "");
                    String deadline = partsLine[1].replaceAll("[^\\d\\-]", "").trim();
                    String status = partsLine[2].trim();

                    PostCardDto dto = PostCardDto.builder()
                            .title(titlePart)
                            .deadline(deadline.length() == 10 ? java.time.LocalDate.parse(deadline) : null)
                            .status(status)
                            .thumbnail(null) // 필요 시 이미지 url 추후 연결
                            .build();

                    result.add(dto);
                }
            }
        }

        return result;
    }
}
