package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SanPhamRepo extends JpaRepository<SanPham, Integer> {
    @Query("SELECT sp FROM SanPham sp WHERE sp.trangThai = true ORDER BY sp.ten DESC")
    List<SanPham> findTopNoiBat(Pageable pageable);

    Page<SanPham> findByTenContainingIgnoreCase(String ten, Pageable pageable);
}
