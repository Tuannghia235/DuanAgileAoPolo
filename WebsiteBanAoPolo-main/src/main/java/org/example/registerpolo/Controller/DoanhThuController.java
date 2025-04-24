package org.example.registerpolo.Controller;

import org.example.registerpolo.Controller.DTO.DTODoanhThu;
import org.example.registerpolo.Entity.ThongKeDoanhThu;
import org.example.registerpolo.Repository.ThongKeDoanhThuRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DoanhThuController {
    @Autowired
    private ThongKeDoanhThuRepo repo;
    //Ngay
    public List<DTODoanhThu> thongKeTheoNgay() {
        YearMonth ym = YearMonth.now();
        int year = ym.getYear();
        int thang = LocalDate.now().getMonthValue();// Tháng hiện tại
        int soNgay = ym.lengthOfMonth();//Ngày trong tháng
        List<ThongKeDoanhThu> list = repo.findAll();
        ArrayList<DTODoanhThu> data = new ArrayList<>();
        for (ThongKeDoanhThu thu : list) {
            if(thu.getNgay().getYear()==year) {
                if (thu.getNgay().getMonthValue() == thang) {
                    if (thu.getNgay().getDayOfMonth() <= soNgay) {
                        DTODoanhThu list1 = new DTODoanhThu(thu.getId(),thu.getNgay(), thu.getTongSoHoaDon(), thu.getTongDoanhThu(), thu.getTongSanPhamBanRa(), thu.getSoLuongSanPhamOffline(), thu.getSoLuongSanPhamOnline());
                        data.add(list1);
                    }
                }
            }
        }
        return data;
    }
    //Thang
    public List<DTODoanhThu> thongKeTheoThang() {
        List<ThongKeDoanhThu> list = repo.findAll();
        Map<YearMonth, DTODoanhThu> map = new LinkedHashMap<>(); // giữ thứ tự theo tháng

        int currentYear = Year.now().getValue();

        for (ThongKeDoanhThu thu : list) {
            LocalDate date = thu.getNgay();
            if (date.getYear() == currentYear) {
                YearMonth ym = YearMonth.from(date); // Lấy tháng và năm

                map.putIfAbsent(ym, new DTODoanhThu(thu.getId(), ym.getMonthValue() + "/" + ym.getYear(), 0, 0.0, 0, 0, 0));

                DTODoanhThu dto = map.get(ym);
                dto.setTongSoHoaDon(dto.getTongSoHoaDon() + thu.getTongSoHoaDon());
                dto.setTongSanPhamBanRa(dto.getTongSanPhamBanRa() + thu.getTongSanPhamBanRa());
                dto.setSoLuongSanPhamOnline(dto.getSoLuongSanPhamOnline() + thu.getSoLuongSanPhamOnline());
                dto.setSoLuongSanPhamOffline(dto.getSoLuongSanPhamOffline() + thu.getSoLuongSanPhamOffline());
                dto.setTongDoanhThu(dto.getTongDoanhThu() + thu.getTongDoanhThu());}
        }
        return new ArrayList<>(map.values());
    }
    //Nam
    public List<DTODoanhThu> thongKeTheoNam() {
        List<ThongKeDoanhThu> list = repo.findAll();
        Map<Integer, DTODoanhThu> map = new LinkedHashMap<>();

        for (ThongKeDoanhThu thu : list) {
            int year = thu.getNgay().getYear();
            map.putIfAbsent(year, new DTODoanhThu(thu.getId(), String.valueOf(year),0, 0.0, 0, 0, 0
            ));
            DTODoanhThu dto = map.get(year);
            dto.setTongSoHoaDon(dto.getTongSoHoaDon() + thu.getTongSoHoaDon());
            dto.setTongSanPhamBanRa(dto.getTongSanPhamBanRa() + thu.getTongSanPhamBanRa());
            dto.setSoLuongSanPhamOnline(dto.getSoLuongSanPhamOnline() + thu.getSoLuongSanPhamOnline());
            dto.setSoLuongSanPhamOffline(dto.getSoLuongSanPhamOffline() + thu.getSoLuongSanPhamOffline());
            dto.setTongDoanhThu(dto.getTongDoanhThu() + thu.getTongDoanhThu());
        }
        return new ArrayList<>(map.values());
    }










    @GetMapping("/doanh-thu")
    public String  getTheoNgay(Model model , RedirectAttributes redirectAttributes) {
        //Ngay
        ArrayList<DTODoanhThu> data = new ArrayList<>();
        data = (ArrayList<DTODoanhThu>) thongKeTheoNgay();
        if (data.get(0).getNgay() == null) {
            redirectAttributes.addFlashAttribute("thong_bao_ngay", "Tháng này chưa bán được gì cả");}
        model.addAttribute("data_ngay", data);


        ArrayList<DTODoanhThu> data2 = new ArrayList<>();
        data2 = (ArrayList<DTODoanhThu>) thongKeTheoThang();
        if (data2.get(0).getNgay() == null) {
            redirectAttributes.addFlashAttribute("thong_bao_ngay", "Tháng này chưa bán được gì cả");
        }
        model.addAttribute("data_thang", data2);


        ArrayList<DTODoanhThu> data3 = new ArrayList<>();
        data3 = (ArrayList<DTODoanhThu>) thongKeTheoNam();
        if (data3.get(0).getNgay() == null) {redirectAttributes.addFlashAttribute("thong_bao_ngay", "Tháng này chưa bán được gì cả");}
        model.addAttribute("data_nam", data3);


        return "doanh_thu/doanh_thu";
    }
}
