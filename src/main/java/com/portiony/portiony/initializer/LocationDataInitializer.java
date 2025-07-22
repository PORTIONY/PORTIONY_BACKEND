package com.portiony.portiony.initializer;

import com.portiony.portiony.entity.Dong;
import com.portiony.portiony.entity.Region;
import com.portiony.portiony.entity.Subregion;
import com.portiony.portiony.repository.DongRepository;
import com.portiony.portiony.repository.RegionRepository;
import com.portiony.portiony.repository.SubregionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LocationDataInitializer implements CommandLineRunner {

    private final RegionRepository regionRepository;
    private final SubregionRepository subregionRepository;
    private final DongRepository dongRepository;

    @Override
    public void run(String... args) throws Exception {
        // 지역 이름 → 엔티티 캐싱 (중복 insert 방지)
        Map<String, Region> regionCache = new HashMap<>();
        Map<String, Subregion> subregionCache = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource("location.csv").getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 3) continue;

                String regionName = tokens[0].trim();
                String subregionName = tokens[1].trim();
                String dongName = tokens[2].trim();

                // Region 캐싱
                Region region = regionCache.computeIfAbsent(regionName, name ->
                        regionRepository.findByCity(name)
                                .orElseGet(() -> regionRepository.save(
                                        Region.builder().city(name).build())));

                // Subregion 캐싱 (key: 시+구 조합)
                String subregionKey = regionName + "_" + subregionName;
                Subregion subregion = subregionCache.computeIfAbsent(subregionKey, key ->
                        subregionRepository.findByDistrictAndRegion(subregionName, region)
                                .orElseGet(() -> subregionRepository.save(
                                        Subregion.builder().district(subregionName).region(region).build())));

                // Dong 저장 (동은 보통 중복 적음, 캐싱 안 해도 됨)
                dongRepository.findByDongAndSubregion(dongName, subregion)
                        .orElseGet(() -> dongRepository.save(
                                Dong.builder().dong(dongName).subregion(subregion).build()));

                count++;
            }

            System.out.println("지역 초기화 완료: 총 " + count + "개 행 처리됨");

        } catch (Exception e) {
            System.err.println("지역 초기화 중 오류 발생: " + e.getMessage());
            throw e;
        }
    }
}
