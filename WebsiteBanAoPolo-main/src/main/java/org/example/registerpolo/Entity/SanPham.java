package org.example.registerpolo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "SanPham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Ma", nullable = false, unique = true)
    private String ma;

    @Column(name = "Ten", nullable = false)
    private String ten;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @ManyToOne
    @JoinColumn(name = "IdThuongHieu")
    private ThuongHieu thuongHieu;

    // Thêm mối quan hệ với SPChiTiet
    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    private List<SPChiTiet> danhSachChiTiet;
}

