package com.portiony.portiony.repository;

import com.portiony.portiony.entity.Dong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import com.portiony.portiony.entity.Subregion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DongRepository extends JpaRepository<Dong, Long> {
    Optional<Dong> findByDongAndSubregion(String dong, Subregion subregion);

    @Query("""
        SELECT d FROM Dong d
        JOIN d.subregion s
        JOIN s.region r
        WHERE REPLACE(CONCAT(r.city, s.district, d.dong), ' ', '') LIKE CONCAT('%', :keyword, '%')
    """)
    Page<Dong> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


    @Query("""
        SELECT d from Dong d JOIN FETCH d.subregion s JOIN FETCH s.region r
        WHERE d.id = :dongId
    """)
    Optional<Dong> findAddressByDongId(@Param("dongId") Long dongId);
}
