package org.example.registerpolo.Repository;



import org.example.registerpolo.Entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KhuyenMaiRepo extends JpaRepository<KhuyenMai, Integer> {
    boolean existsByMaKM(String maKM);
    List<KhuyenMai> findAllByTrangThaiTrue();

    KhuyenMai findByMaKM(String maKM); // Nếu bạn cần lấy theo mã KM
}
