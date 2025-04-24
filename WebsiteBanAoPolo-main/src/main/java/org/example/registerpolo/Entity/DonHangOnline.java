package org.example.registerpolo.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "DonHangOnline")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonHangOnline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "MaDonHang", unique = true)
    private String maDonHang;

    @ManyToOne
    @JoinColumn(name = "IdKhachHang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "IdKhuyenMai")
    private KhuyenMai khuyenMai;

    @Column(name = "NgayDatHang")
    private LocalDateTime ngayDatHang;

    @Column(name = "DiaChiGiaoHang")
    private String diaChiGiaoHang;

    @Column(name = "PhiShip")
    private BigDecimal phiShip;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "HinhThucThanhToan")
    private String hinhThucThanhToan;
    public BigDecimal getTongTienSauGiamGia() {
        BigDecimal tongTienSauGiamGia = this.tongTien.subtract(this.khuyenMai != null ? this.khuyenMai.getPhanTramGiam().multiply(this.tongTien).divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
        BigDecimal tongTienSauDiemTichLuy = tongTienSauGiamGia.subtract(this.khachHang.getDiemTichLuy() != null ? BigDecimal.valueOf(this.khachHang.getDiemTichLuy()) : BigDecimal.ZERO);
        return tongTienSauDiemTichLuy.add(this.phiShip); // Cộng thêm phí ship
    }
}
