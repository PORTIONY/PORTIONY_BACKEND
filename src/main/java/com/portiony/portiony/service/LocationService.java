package com.portiony.portiony.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.portiony.portiony.dto.location.LocationDetailResponseDto;
import com.portiony.portiony.dto.location.LocationSearchResponseDto;
import com.portiony.portiony.dto.location.ResolvedLocationResponseDto;
import com.portiony.portiony.entity.Dong;
import com.portiony.portiony.entity.Region;
import com.portiony.portiony.entity.Subregion;
import com.portiony.portiony.repository.DongRepository;
import com.portiony.portiony.repository.RegionRepository;
import com.portiony.portiony.repository.SubregionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final RegionRepository regionRepository;
    private final SubregionRepository subregionRepository;
    private final DongRepository dongRepository;

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    public ResolvedLocationResponseDto resolveRegionIds(double lat, double lng, int page, int size) {
        // 카카오 API 호출
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

        String fullAddress = regionName + " " + subregionName + " " + dongName;

        return new ResolvedLocationResponseDto(fullAddress, searchLocations(fullAddress, page, size));
    }

    public List<LocationSearchResponseDto> searchLocations(String keyword, int page, int size) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("키워드 누락입니다.");
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        String cleanedKeyword = keyword.replaceAll("\\s+", ""); //모든 공백 제거

        Page<Dong> dongs = dongRepository.searchByKeyword(cleanedKeyword, pageable);

        return dongs.stream()
                .map(d -> {
                    var s = d.getSubregion();
                    var r = s.getRegion();
                    String address = r.getCity() + " " + s.getDistrict() + " " + d.getDong();
                    return LocationSearchResponseDto.builder()
                            .regionId(r.getId())
                            .subregionId(s.getId())
                            .dongId(d.getId())
                            .address(address)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public LocationDetailResponseDto getByDongId(Long dongId) {
        Dong dong = dongRepository.findAddressByDongId(dongId)
                .orElseThrow(() -> new IllegalArgumentException("해당 동이 존재하지 않습니다. dongId=" + dongId));

        Subregion subregion = dong.getSubregion();
        Region region = subregion.getRegion();

        String address = region.getCity() + " " + subregion.getDistrict() + " " + dong.getDong();

        return LocationDetailResponseDto.builder()
                .regionId(region.getId())
                .regionName(region.getCity())
                .subregionId(subregion.getId())
                .subregionName(subregion.getDistrict())
                .dongId(dong.getId())
                .dongName(dong.getDong())
                .address(address)
                .build();
    }
}
