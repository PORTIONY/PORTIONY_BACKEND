package com.portiony.portiony.initializer;

import com.portiony.portiony.entity.Dong;
import com.portiony.portiony.entity.Region;
import com.portiony.portiony.entity.Subregion;
import com.portiony.portiony.repository.DongRepository;
import com.portiony.portiony.repository.RegionRepository;
import com.portiony.portiony.repository.SubregionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnProperty(prefix = "portiony.location.init", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class LocationDataInitializer implements CommandLineRunner {

    private final RegionRepository regionRepository;
    private final SubregionRepository subregionRepository;
    private final DongRepository dongRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (alreadyInitialized()) {
            System.out.println("LocationDataInitializer: 데이터 존재하므로 스킵합니다..");
            return;
        }

        System.out.println(" LocationDataInitializer 시작");

        Map<String, Region> regionCache = new HashMap<>();
        Map<String, Subregion> subregionCache = new HashMap<>();

        AtomicInteger regionCount = new AtomicInteger();
        AtomicInteger subregionCount = new AtomicInteger();
        AtomicInteger dongCount = new AtomicInteger();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource("location.csv").getInputStream(), StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) continue;

                String[] tokens = line.split(",",-1);
                if (tokens.length < 3) continue;

                String regionName = tokens[0].trim();
                String subregionName = tokens[1].trim();
                String dongName = tokens[2].trim();

                if (regionName.isBlank()) continue;

                // Region 저장
                Region region = regionCache.computeIfAbsent(regionName, name -> {
                    return regionRepository.findByCity(name)
                            .orElseGet(() -> {
                                System.out.println("새 Region 저장: " + name);
                                regionCount.incrementAndGet();
                                return regionRepository.save(Region.builder().city(name).build());
                            });
                });

                // Subregion, Dong 없이 끝
                if (subregionName.isBlank() || dongName.isBlank()) continue;

                // Subregion 저장
                String subregionKey = regionName + "_" + subregionName;
                Subregion subregion = subregionCache.computeIfAbsent(subregionKey, key ->
                        subregionRepository.findByDistrictAndRegion(subregionName, region)
                                .orElseGet(() -> {
                                    subregionCount.incrementAndGet();
                                    return subregionRepository.save(
                                            Subregion.builder().district(subregionName).region(region).build());
                                })
                );


                // Dong 저장
                dongRepository.findByDongAndSubregion(dongName, subregion)
                        .orElseGet(() -> {
                            dongCount.incrementAndGet();
                            return dongRepository.save(
                                    Dong.builder().dong(dongName).subregion(subregion).build());
                        });
            }

            System.out.println("지역 초기화 완료");
            System.out.println("   - 저장된 Region 수: " + regionCount.get());
            System.out.println("   - 저장된 Subregion 수: " + subregionCount.get());
            System.out.println("   - 저장된 Dong 수: " + dongCount.get());

        } catch (Exception e) {
            System.err.println("지역 초기화 중 오류 발생: " + e.getMessage());
            throw e;
        }
    }

    private boolean alreadyInitialized() {
        return regionRepository.count() > 0
                || subregionRepository.count() > 0
                || dongRepository.count() > 0;
    }
}
