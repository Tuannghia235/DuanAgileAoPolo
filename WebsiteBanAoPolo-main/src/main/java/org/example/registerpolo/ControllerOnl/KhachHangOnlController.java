package org.example.registerpolo.ControllerOnl;

import jakarta.servlet.http.HttpSession;
import org.example.registerpolo.Entity.KhachHang;
import org.example.registerpolo.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ralph-lauren")
public class KhachHangOnlController {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @PostMapping("/dang-nhap")
    public String dangNhap(@RequestParam String email,
                           @RequestParam String matKhau,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        KhachHang khachHang = khachHangRepository.findByEmailAndMatKhau(email, matKhau);

        if (khachHang != null) {
            session.setAttribute("khachHang", khachHang);
            session.setAttribute("idKhachHang", khachHang.getId()); // ✅ Bổ sung dòng này
            return "redirect:/ralph-lauren/trang-chu";
        } else {
            redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng!");
            return "redirect:/ralph-lauren/trang-chu";
        }
    }


    @GetMapping("/dang-xuat")
    public String dangXuat(HttpSession session) {
        session.removeAttribute("khachHang"); // Xóa thông tin khách hàng khỏi session
        return "redirect:/ralph-lauren/trang-chu"; // Quay về trang chủ
    }

    @PostMapping("/dang-ky")
    public String dangKy(@RequestParam String hoTen,
                         @RequestParam String email,
                         @RequestParam String matKhau,
                         @RequestParam String sdt,
                         Model model) {

        // Kiểm tra xem email đã tồn tại hay chưa
        if (khachHangRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email này đã được đăng ký!");
            model.addAttribute("showRegisterModal", true);  // Mở lại modal đăng ký
            return "BanHangOnl/TrangChuOnl.html";  // Quay lại trang chủ hoặc trang có modal đăng ký
        }

        // Tạo mới khách hàng
        KhachHang khachHang = new KhachHang();
        khachHang.setTen(hoTen);
        khachHang.setEmail(email);
        khachHang.setMatKhau(matKhau);  // Lưu mật khẩu chưa mã hóa (nếu chưa mã hóa mật khẩu)
        khachHang.setSdt(sdt);
        khachHang.setTrangThai(true);  // Mặc định khách hàng ở trạng thái hoạt động

        khachHangRepository.save(khachHang);  // Lưu vào database

        model.addAttribute("success", "Đăng ký thành công!");  // Thông báo đăng ký thành công
        return "BanHangOnl/TrangChuOnl.html";  // Quay lại trang chủ
    }
    @GetMapping("/thong-tin-khach-hang")
    public String hienThiThongTin(HttpSession session, Model model) {
        KhachHang khachHang = (KhachHang) session.getAttribute("khachHang");
        if (khachHang == null) {
            return "redirect:/ralph-lauren/trang-chu"; // nếu chưa đăng nhập thì quay lại trang chủ
        }
        return "BanHangOnl/ThongTinCaNhan";
    }

}