package org.example.registerpolo.ControllerOnl;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.registerpolo.Entity.*;
import org.example.registerpolo.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ralph-lauren")
public class DonHangOnlineController {
    @Autowired
    private DonHangOnlineRepo donHangOnlineRepo;
    @Autowired
    private GioHangRepo gioHangRepo;
    @Autowired
    private GioHangChiTietRepo gioHangChiTietRepo;
    @Autowired
    private KhachHangRepository khachHangRepo;
    @Autowired
    private DonHangOnlineChiTietRepo donHangOnlineChiTietRepo;
    @Autowired
    private KhuyenMaiRepo khuyenMaiRepo;
    /**
     * Hiển thị trang thanh toán với thông tin đơn hàng
     */
    @GetMapping("/thanh-toan")
    public String hienThiChiTietDonHang(HttpSession session, Model model) {
        KhachHang khachHang = (KhachHang) session.getAttribute("khachHang");
        if (khachHang == null) return "redirect:/ralph-lauren/gio-hang";

        Optional<GioHang> gioHangOpt = gioHangRepo.findByKhachHangAndTrangThai(khachHang, false);
        if (gioHangOpt.isEmpty()) return "redirect:/ralph-lauren/gio-hang";

        GioHang gioHang = gioHangOpt.get();
        List<GioHangChiTiet> dsSanPham = gioHangChiTietRepo.findByGioHang(gioHang);

        BigDecimal tongTienHang = dsSanPham.stream()
                .map(sp -> sp.getSpChiTiet().getDonGia().multiply(BigDecimal.valueOf(sp.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<KhuyenMai> dsKhuyenMai = khuyenMaiRepo.findAllByTrangThaiTrue();

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("gioHang", gioHang);
        model.addAttribute("dsSanPham", dsSanPham);
        model.addAttribute("tongTienHang", tongTienHang);
        model.addAttribute("diemTichLuy", khachHang.getDiemTichLuy());
        model.addAttribute("dsKhuyenMai", dsKhuyenMai);
        model.addAttribute("ngayDatHang", LocalDateTime.now());

        return "BanHangOnl/don-hang-chi-tiet";
    }


    @PostMapping("/xac-nhan-thanh-toan")
    public String xacNhanThanhToan(
            HttpSession session,
            @RequestParam("diaChiGiaoHang") String diaChiGiaoHang,
            @RequestParam("hinhThucThanhToan") String hinhThucThanhToan,
            @RequestParam(name = "maKhuyenMai", required = false) String maKhuyenMai,
            @RequestParam(name = "diemSuDung", defaultValue = "0") int diemSuDung
    ) {
        KhachHang khachHang = (KhachHang) session.getAttribute("khachHang");
        if (khachHang == null) return "redirect:/ralph-lauren/gio-hang";

        Optional<GioHang> gioHangOpt = gioHangRepo.findByKhachHangAndTrangThai(khachHang, false);
        if (gioHangOpt.isEmpty()) return "redirect:/ralph-lauren/gio-hang";

        GioHang gioHang = gioHangOpt.get();
        List<GioHangChiTiet> dsSP = gioHangChiTietRepo.findByGioHang(gioHang);

        BigDecimal tongTien = dsSP.stream()
                .map(sp -> sp.getSpChiTiet().getDonGia().multiply(BigDecimal.valueOf(sp.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Áp dụng khuyến mãi
        BigDecimal giamGia = BigDecimal.ZERO;
        KhuyenMai khuyenMai = null;
        if (maKhuyenMai != null && !maKhuyenMai.isBlank()) {
            khuyenMai = khuyenMaiRepo.findByMaKM(maKhuyenMai);
            if (khuyenMai != null && khuyenMai.getPhanTramGiam() != null && tongTien != null) {
                BigDecimal phanTram = khuyenMai.getPhanTramGiam();
                giamGia = tongTien.multiply(phanTram).divide(BigDecimal.valueOf(100));
            }

        }

        // Áp dụng điểm tích lũy
        int diemHienCo = khachHang.getDiemTichLuy() != null ? khachHang.getDiemTichLuy() : 0;
        if (diemSuDung > diemHienCo) diemSuDung = diemHienCo;

        BigDecimal tienGiamDiem = BigDecimal.valueOf(diemSuDung * 1000L);
        BigDecimal tienPhaiThanhToan = tongTien.subtract(giamGia).subtract(tienGiamDiem);
        if (tienPhaiThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            tienPhaiThanhToan = BigDecimal.ZERO;
        }

        // Tạo đơn hàng
        DonHangOnline donHang = new DonHangOnline();
        donHang.setKhachHang(khachHang);
        donHang.setDiaChiGiaoHang(diaChiGiaoHang);
        donHang.setHinhThucThanhToan(hinhThucThanhToan);
        donHang.setNgayDatHang(LocalDateTime.now());
        donHang.setTongTien(tienPhaiThanhToan);
        donHang.setKhuyenMai(khuyenMai);
        donHang.setTrangThai("CHỜ XÁC NHẬN");
        donHangOnlineRepo.save(donHang);

        // Chi tiết đơn hàng
        for (GioHangChiTiet ghct : dsSP) {
            DonHangOnlineChiTiet ct = new DonHangOnlineChiTiet();
            ct.setDonHangOnline(donHang);
            ct.setSpChiTiet(ghct.getSpChiTiet());
            ct.setSoLuong(ghct.getSoLuong());
            ct.setDonGia(ghct.getSpChiTiet().getDonGia());
            donHangOnlineChiTietRepo.save(ct);
        }

        // Trừ điểm tích lũy
        khachHang.setDiemTichLuy(diemHienCo - diemSuDung);
        khachHangRepo.save(khachHang);

        // Xóa giỏ hàng
        gioHangChiTietRepo.deleteAll(dsSP);
        gioHangRepo.delete(gioHang);

        return "redirect:/ralph-lauren/lich-su-don-hang";
    }

}
