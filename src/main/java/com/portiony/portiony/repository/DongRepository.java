package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Dong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.portiony.portiony.entity.Subregion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DongRepository extends JpaRepository<Dong, Long> {
    Optional<Dong> findByDongAndSubregion(String dong, Subregion subregion);

    @Query("""
        SELECT d FROM Dong d JOIN d.subregion s JOIN s.region r
        WHERE LOWER(d.dong)     LIKE LOWER(CONCAT('%', :k, '%'))
            OR LOWER(s.district)  LIKE LOWER(CONCAT('%', :k, '%'))
            OR LOWER(r.city)      LIKE LOWER(CONCAT('%', :k, '%'))
        ORDER BY r.city, s.district, d.dong
    """)
    Page<Dong> searchByKeyword(@Param("k") String keyword, Pageable pageable);
}
