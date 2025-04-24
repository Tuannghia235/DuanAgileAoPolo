package org.example.registerpolo.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Getter
@Setter
@Entity
@Table(name="ThongKeDoanhThu")
public class ThongKeDoanhThu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;


    @Column(name = "Ngay")
    private LocalDate ngay;


    @Column(name = "TongSoHoaDon")
    private Integer tongSoHoaDon;


    @Column(name = "TongDoanhThu")
    private Double tongDoanhThu;

    @Column(name = "TongSanPhamBanRa")
    private Integer tongSanPhamBanRa;


    @Column(name = "SoLuongSanPhamOffline")
    private Integer soLuongSanPhamOffline;


    @Column(name = "SoLuongSanPhamOnline")
    private Integer soLuongSanPhamOnline;


}