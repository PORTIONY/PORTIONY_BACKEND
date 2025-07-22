package com.portiony.portiony.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.portiony.portiony.dto.LocationResponseDto;
import com.portiony.portiony.entity.Dong;
import com.portiony.portiony.entity.Region;
import com.portiony.portiony.entity.Subregion;
import com.portiony.portiony.repository.DongRepository;
import com.portiony.portiony.repository.RegionRepository;
import com.portiony.portiony.repository.SubregionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final RegionRepository regionRepository;
    private final SubregionRepository subregionRepository;
    private final DongRepository dongRepository;

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    public LocationResponseDto resolveRegionIds(double lat, double lng) {
        String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=" + lng + "&y=" + lat;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode documents = response.getBody().get("documents");
        if (documents == null || documents.isEmpty()) {
            throw new RuntimeException("주소 정보 없음");
        }

        JsonNode info = documents.get(0);
        String regionName = info.get("region_1depth_name").asText();     // ex: 서울특별시
        String subregionName = info.get("region_2depth_name").asText();  // ex: 강남구
        String dongName = info.get("region_3depth_name").asText();       // ex: 역삼동

        Region region = regionRepository.findByCity(regionName)
                .orElseThrow(() -> new IllegalArgumentException("해당 region 없음"));

        Subregion subregion = subregionRepository.findByDistrictAndRegion(subregionName, region)
                .orElseThrow(() -> new IllegalArgumentException("해당 subregion 없음"));

        Dong dong = dongRepository.findByDongAndSubregion(dongName, subregion)
                .orElseThrow(() -> new IllegalArgumentException("해당 동 없음"));

        return new LocationResponseDto(region.getId(), subregion.getId(), dong.getId());
    }
}
