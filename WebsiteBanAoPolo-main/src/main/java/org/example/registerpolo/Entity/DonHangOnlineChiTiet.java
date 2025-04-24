package org.example.registerpolo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "DonHangOnlineChiTiet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonHangOnlineChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "IdDonHangOnline")
    private DonHangOnline donHangOnline;

    @ManyToOne
    @JoinColumn(name = "IdSPCT")
    private SPChiTiet spChiTiet;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "DonGia")
    private BigDecimal donGia;
}
