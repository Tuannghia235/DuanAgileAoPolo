package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    @Query("SELECT kh FROM KhachHang kh ORDER BY kh.id DESC LIMIT 1")
    KhachHang findLastKhachHang();
    KhachHang findBySdt(String sdt);
    KhachHang findByEmailAndMatKhau(String email, String matKhau);
    boolean existsByEmail(String email); // Kiểm tra xem email đã tồn tại hay chưa
    KhachHang findByEmail(String email);  // Tìm khách hàng theo email
}
