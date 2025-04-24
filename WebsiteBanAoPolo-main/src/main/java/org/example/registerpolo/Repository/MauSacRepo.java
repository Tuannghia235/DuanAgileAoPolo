package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MauSacRepo extends JpaRepository<MauSac, Integer> {
    @Query("SELECT DISTINCT m.ten FROM MauSac m")
    List<String> findAllDistinctTenMau();
    @Query("SELECT COUNT(m) > 0 FROM MauSac m WHERE m.ma = :ma")
    boolean existsByMa(@Param("ma") String ma);
    List<MauSac> findByTrangThaiTrue();
}
