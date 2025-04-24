package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.HinhAnhSPChiTiet;
import org.example.registerpolo.Entity.SPChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HinhAnhSPChiTietRepo extends JpaRepository<HinhAnhSPChiTiet, Integer> {
    List<HinhAnhSPChiTiet> findBySpChiTiet(SPChiTiet spChiTiet);
    List<HinhAnhSPChiTiet> findBySpChiTiet_Id(Integer spChiTietId);

}
