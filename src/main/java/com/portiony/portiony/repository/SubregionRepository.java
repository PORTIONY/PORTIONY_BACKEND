package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Subregion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.portiony.portiony.entity.Region;


public interface SubregionRepository extends JpaRepository<Subregion, Long> {
    Optional<Subregion> findByDistrictAndRegion(String district, Region region);
}
