package org.example.registerpolo.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "KhachHang")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Ten")
    private String ten;

    @Column(name = "SDT")
    private String sdt;

    @Column(name = "Email")
    private String email;

    @Column(name = "MaKH")
    private String maKH;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @Column(name = "DiemTichLuy") // 👈 Thêm cột tích điểm
    private Integer diemTichLuy = 0; // Mặc định là 0

    // Thêm thuộc tính mật khẩu
    @Column(name = "MatKhau")
    private String matKhau;  // Mật khẩu người dùng
}
