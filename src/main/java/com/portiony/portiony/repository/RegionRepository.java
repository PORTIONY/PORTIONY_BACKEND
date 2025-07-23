package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByCity(String city);
}
