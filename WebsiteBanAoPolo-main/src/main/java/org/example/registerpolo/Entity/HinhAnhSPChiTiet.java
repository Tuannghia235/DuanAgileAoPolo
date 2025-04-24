package org.example.registerpolo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HinhAnhSPChiTiet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HinhAnhSPChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "IdSPChiTiet")
    private SPChiTiet spChiTiet;

    @Column(name = "Url")
    private String url;

    @Column(name = "MoTa")
    private String moTa;

    @Column(name = "TrangThai")
    private Boolean trangThai;
}
