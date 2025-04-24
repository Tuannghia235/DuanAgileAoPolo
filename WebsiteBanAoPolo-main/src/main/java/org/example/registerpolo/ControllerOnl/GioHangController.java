package org.example.registerpolo.ControllerOnl;

import org.example.registerpolo.Entity.GioHang;
import org.example.registerpolo.Entity.GioHangChiTiet;
import org.example.registerpolo.Entity.KhachHang;
import org.example.registerpolo.Entity.SPChiTiet;
import org.example.registerpolo.Repository.GioHangChiTietRepo;
import org.example.registerpolo.Repository.GioHangRepo;
import org.example.registerpolo.Repository.SPChiTietRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/ralph-lauren")
public class GioHangController {
    @Autowired
    private SPChiTietRepo spChiTietRepo;
    @Autowired
    private GioHangRepo gioHangRepository;

    @Autowired
    private GioHangChiTietRepo gioHangChiTietRepository;

    @GetMapping("/gio-hang")
    public String viewGioHang(HttpSession session, Model model) {
        // Lấy ID khách hàng từ session
        Integer idKhachHang = (Integer) session.getAttribute("idKhachHang");

        // Kiểm tra nếu khách hàng chưa đăng nhập
        if (idKhachHang == null) {
            model.addAttribute("message", "Vui lòng đăng nhập để xem giỏ hàng.");
            return "BanHangOnl/GioHang";  // Trả về trang giỏ hàng nếu chưa đăng nhập
        }

        // Lấy giỏ hàng của khách hàng
        GioHang gioHang = gioHangRepository.findByKhachHang_Id(idKhachHang);
        if (gioHang == null) {
            model.addAttribute("message", "Giỏ hàng trống.");
            model.addAttribute("dsSanPham", List.of()); // Tránh lỗi Thymeleaf khi giỏ hàng trống
            model.addAttribute("soLuongSanPham", 0);  // Nếu giỏ hàng trống, hiển thị số lượng sản phẩm là 0
            return "BanHangOnl/GioHang";
        }

        // Lấy danh sách sản phẩm trong giỏ hàng
        List<GioHangChiTiet> dsSanPham = gioHangChiTietRepository.findByGioHang_Id(gioHang.getId());

        // Tính tổng tiền trong giỏ hàng
        BigDecimal tongTien = dsSanPham.stream()
                .map(sp -> sp.getSpChiTiet().getDonGia().multiply(BigDecimal.valueOf(sp.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính tổng số lượng sản phẩm trong giỏ hàng
        int soLuongSanPham = dsSanPham.stream()
                .mapToInt(GioHangChiTiet::getSoLuong)
                .sum();

        // Cập nhật thông tin giỏ hàng vào model
        model.addAttribute("gioHang", gioHang);
        model.addAttribute("dsSanPham", dsSanPham);
        model.addAttribute("tongTien", tongTien);
        model.addAttribute("soLuongSanPham", soLuongSanPham);  // Truyền tổng số lượng sản phẩm vào view

        return "BanHangOnl/GioHang";  // Trả về trang giỏ hàng với dữ liệu cập nhật
    }

    @PostMapping("/gio-hang/them")
    public String themVaoGioHang(
            @RequestParam("spChiTietId") Integer spChiTietId,
            @RequestParam("soLuong") int soLuong,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        KhachHang kh = (KhachHang) session.getAttribute("khachHang");
        if (kh == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm sản phẩm.");
            return "redirect:/login";
        }

        // Lấy giỏ hàng hoặc tạo mới nếu chưa có
        GioHang gioHang = gioHangRepository.findByKhachHangId(kh.getId())
                .orElseGet(() -> {
                    GioHang gh = new GioHang();
                    gh.setKhachHang(kh);
                    gh.setTrangThai(false);
                    gh.setNgayTao(LocalDateTime.now());
                    return gioHangRepository.save(gh);
                });

        // Lấy sản phẩm chi tiết
        SPChiTiet spChiTiet = spChiTietRepo.findById(spChiTietId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Kiểm tra tồn kho
        if (spChiTiet.getSoLuong() < soLuong) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không đủ tồn kho.");
            return "redirect:/ralph-lauren/san-pham/" + spChiTietId;
        }

        // Cập nhật tồn kho
        spChiTiet.setSoLuong(spChiTiet.getSoLuong() - soLuong);
        spChiTietRepo.save(spChiTiet);

        // Kiểm tra sản phẩm đã tồn tại trong giỏ
        Optional<GioHangChiTiet> chiTietOpt = gioHangChiTietRepository
                .findByGioHangIdAndSpChiTietId(gioHang.getId(), spChiTietId);

        if (chiTietOpt.isPresent()) {
            GioHangChiTiet ct = chiTietOpt.get();
            ct.setSoLuong(ct.getSoLuong() + soLuong);
            gioHangChiTietRepository.save(ct);
        } else {
            GioHangChiTiet ct = new GioHangChiTiet();
            ct.setGioHang(gioHang);
            ct.setSpChiTiet(spChiTiet);
            ct.setSoLuong(soLuong);
            gioHangChiTietRepository.save(ct);
        }

        redirectAttributes.addFlashAttribute("success", "Đã thêm vào giỏ hàng.");
        return "redirect:/ralph-lauren/san-pham/" + spChiTietId;
    }

    @PostMapping("/gio-hang/cap-nhat")
    public String capNhatSoLuongSanPham(
            @RequestParam("idGioHangCT") Integer idGioHangCT,
            @RequestParam("soLuongMoi") int soLuongMoi,
            RedirectAttributes redirectAttributes) {

        GioHangChiTiet gioHangChiTiet = gioHangChiTietRepository.findById(idGioHangCT)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        SPChiTiet spChiTiet = gioHangChiTiet.getSpChiTiet();
        int soLuongCu = gioHangChiTiet.getSoLuong();

        if (soLuongMoi == 0) {
            // Xóa khỏi giỏ nếu số lượng = 0, cộng lại tồn kho
            spChiTiet.setSoLuong(spChiTiet.getSoLuong() + soLuongCu);
            spChiTietRepo.save(spChiTiet);
            gioHangChiTietRepository.deleteById(idGioHangCT);

            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
        } else {
            int chenhlech = soLuongMoi - soLuongCu;

            if (chenhlech > 0 && spChiTiet.getSoLuong() < chenhlech) {
                redirectAttributes.addFlashAttribute("error", "Không đủ tồn kho để cập nhật.");
                return "redirect:/ralph-lauren/gio-hang";
            }

            // Cập nhật tồn kho
            spChiTiet.setSoLuong(spChiTiet.getSoLuong() - chenhlech);
            spChiTietRepo.save(spChiTiet);

            // Cập nhật giỏ
            gioHangChiTiet.setSoLuong(soLuongMoi);
            gioHangChiTietRepository.save(gioHangChiTiet);

            redirectAttributes.addFlashAttribute("success", "Cập nhật số lượng thành công.");
        }

        return "redirect:/ralph-lauren/gio-hang";
    }
    // Xử lý xóa sản phẩm khỏi giỏ hàng
    @PostMapping("/gio-hang/xoa")
    public String xoaSanPhamKhoiGioHang(@RequestParam("idSPCT") Integer idSPCT,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        Integer idKhachHang = (Integer) session.getAttribute("idKhachHang");

        if (idKhachHang == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xóa sản phẩm.");
            return "redirect:/login";
        }

        // Tìm giỏ hàng của khách hàng
        Optional<GioHang> gioHangOpt = gioHangRepository.findByKhachHangId(idKhachHang);
        if (!gioHangOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn không tồn tại.");
            return "redirect:/ralph-lauren/gio-hang";
        }

        GioHang gioHang = gioHangOpt.get();

        // Tìm sản phẩm trong giỏ hàng chi tiết dựa trên idSPCT và giỏ hàng
        Optional<GioHangChiTiet> gioHangChiTietOpt =
                gioHangChiTietRepository.findByGioHang_IdAndSpChiTiet_Id(gioHang.getId(), idSPCT);

        if (gioHangChiTietOpt.isPresent()) {
            GioHangChiTiet gioHangChiTiet = gioHangChiTietOpt.get();

            // Cập nhật tồn kho
            SPChiTiet spChiTiet = gioHangChiTiet.getSpChiTiet();
            spChiTiet.setSoLuong(spChiTiet.getSoLuong() + gioHangChiTiet.getSoLuong());
            spChiTietRepo.save(spChiTiet);

            // Xóa khỏi giỏ hàng
            gioHangChiTietRepository.delete(gioHangChiTiet);
            gioHangChiTietRepository.flush();

            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại trong giỏ hàng.");
        }

        return "redirect:/ralph-lauren/gio-hang";
    }

}
