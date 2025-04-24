package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThuongHieuRepo extends JpaRepository<ThuongHieu, Integer> {
    @Query("SELECT COUNT(t) > 0 FROM ThuongHieu t WHERE t.ten = :ten")
    boolean existsByTen(@Param("ten") String ten);
    List<ThuongHieu> findByTrangThaiTrue();
}
