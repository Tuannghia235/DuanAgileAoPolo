package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.GioHang;
import org.example.registerpolo.Entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangChiTietRepo extends JpaRepository<GioHangChiTiet, Integer> {
    List<GioHangChiTiet> findByGioHang_Id(Integer idGioHang);
    Optional<GioHangChiTiet> findByGioHangIdAndSpChiTietId(Integer gioHangId, Integer spChiTietId);
//    GioHangChiTiet findByGioHang_IdAndSpChiTiet_Id(Integer idGioHang, Integer idSPCT);

    Optional<GioHangChiTiet> findByGioHang_IdAndSpChiTiet_Id(Integer gioHangId, Integer spChiTietId);
    void deleteAllByGioHangId(Integer idGioHang);
    void delete(GioHangChiTiet gioHangChiTiet);
    List<GioHangChiTiet> findByGioHang(GioHang gioHang);
    void deleteAllByGioHang(GioHang gioHang);
}
