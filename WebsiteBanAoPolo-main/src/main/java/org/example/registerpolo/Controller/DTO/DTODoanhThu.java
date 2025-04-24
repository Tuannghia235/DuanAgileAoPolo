package org.example.registerpolo.Controller.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DTODoanhThu {

    private Integer id;
    private Object ngay;
    private Integer tongSoHoaDon;
    private Double tongDoanhThu;
    private Integer tongSanPhamBanRa;
    private Integer soLuongSanPhamOffline;
    private Integer soLuongSanPhamOnline;

}