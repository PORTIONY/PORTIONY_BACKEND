package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Dong;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.portiony.portiony.entity.Subregion;

public interface DongRepository extends JpaRepository<Dong, Long> {
    Optional<Dong> findByDongAndSubregion(String dong, Subregion subregion);
}
