package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.GioHang;
import org.example.registerpolo.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepo extends JpaRepository<GioHang, Integer> {
    GioHang findByKhachHang_Id(Integer idKhachHang);
    Optional<GioHang> findByKhachHangId(Integer khachHangId);
    Optional<GioHang> findByKhachHangAndTrangThai(KhachHang khachHang, Boolean trangThai);

}
