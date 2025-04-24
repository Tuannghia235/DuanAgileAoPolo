package org.example.registerpolo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "SPChiTiet")
public class SPChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "MaSPCT", nullable = false, unique = true)
    private String maSPCT;

    @ManyToOne
    @JoinColumn(name = "IdMauSac")
    private MauSac mauSac;

    @ManyToOne
    @JoinColumn(name = "IdKichThuoc")
    private KichThuoc kichThuoc;

    @ManyToOne
    @JoinColumn(name = "IdSanPham")
    private SanPham sanPham;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "DonGia")
    private BigDecimal donGia;

    @Column(name = "TrangThai")
    private Boolean trangThai;
    @OneToMany(mappedBy = "spChiTiet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HinhAnhSPChiTiet> hinhAnhList;

    // Thêm thuộc tính hình ảnh (danh sách hình ảnh)
//    @Transient
//    private List<HinhAnhSPChiTiet> hinhAnhList; // Danh sách hình ảnh

    // Getter cho tên sản phẩm từ SanPham
    public String getTen() {
        return sanPham != null ? sanPham.getTen() : "Tên sản phẩm không có";
    }
}
