package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.KhuyenMai;
import org.example.registerpolo.Entity.ThuongHieu;
import org.example.registerpolo.Repository.KhuyenMaiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Thêm import này

import java.util.List;
import java.util.Optional; // Thêm import này


@Controller
@RequestMapping("/khuyen-mai")
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiRepo khuyenMaiRepo;

    @GetMapping
    public String danhSach(Model model) {
        List<KhuyenMai> list = khuyenMaiRepo.findAll();
        model.addAttribute("khuyenMais", list);
        return "khuyenMai/index";
    }

    @GetMapping("/them")
    public String hienFormThem(Model model) {
        model.addAttribute("khuyenMai", new KhuyenMai());
        return "khuyenMai/form";
    }

    @PostMapping("/luu")
    public String luu(@ModelAttribute("khuyenMai") KhuyenMai khuyenMai,
                      Model model,
                      RedirectAttributes redirectAttributes) { // Thêm RedirectAttributes

        // --- Biến để lưu thông báo lỗi ---
        String errorMessage = null;

        // --- Validation Mã KM đã tồn tại ---
        // Kiểm tra chỉ khi thêm mới HOẶC khi cập nhật và mã KM bị thay đổi
        if (khuyenMai.getId() == null) { // Thêm mới
            if (khuyenMaiRepo.existsByMaKM(khuyenMai.getMaKM())) {
                errorMessage = "Mã khuyến mãi '" + khuyenMai.getMaKM() + "' đã tồn tại.";
            }
        } else { // Cập nhật
            // Lấy thông tin khuyến mãi hiện tại từ DB
            Optional<KhuyenMai> existingKhuyenMaiOpt = khuyenMaiRepo.findById(khuyenMai.getId());
            if (existingKhuyenMaiOpt.isPresent()) {
                KhuyenMai existingKhuyenMai = existingKhuyenMaiOpt.get();
                // Nếu mã KM bị thay đổi
                if (!existingKhuyenMai.getMaKM().equals(khuyenMai.getMaKM())) {
                    // Kiểm tra xem mã KM mới này đã tồn tại cho khuyến mãi khác chưa
                    if (khuyenMaiRepo.existsByMaKM(khuyenMai.getMaKM())) {
                        errorMessage = "Mã khuyến mãi '" + khuyenMai.getMaKM() + "' đã tồn tại cho khuyến mãi khác.";
                    }
                }
            }
            // Lưu ý: Nếu không tìm thấy ID khi cập nhật, sẽ không có lỗi mã KM ở đây,
            // nhưng save() có thể gặp vấn đề hoặc bạn nên xử lý trường hợp không tìm thấy ID trước đó.
            // Trong ví dụ này, chúng ta tập trung vào validation.
        }


        // --- Validation Ngày bắt đầu <= Ngày kết thúc ---
        if (errorMessage == null && khuyenMai.getNgayBatDau() != null && khuyenMai.getNgayKetThuc() != null) {
            if (khuyenMai.getNgayBatDau().isAfter(khuyenMai.getNgayKetThuc())) {
                errorMessage = "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.";
            }
        } else if (errorMessage == null && (khuyenMai.getNgayBatDau() == null || khuyenMai.getNgayKetThuc() == null)) {
            // Kiểm tra nếu ngày bị null và chưa có lỗi nào khác
            errorMessage = "Ngày bắt đầu và Ngày kết thúc không được để trống.";
        }


        // --- Xử lý kết quả Validation ---
        if (errorMessage != null) {
            // Có lỗi validation
            model.addAttribute("errorMessage", errorMessage);
            // Giữ lại dữ liệu đã nhập để hiển thị trên form
            model.addAttribute("khuyenMai", khuyenMai);
            return "khuyenMai/form"; // Trả về lại form để hiển thị lỗi
        } else {
            // Không có lỗi validation, tiến hành lưu
            khuyenMaiRepo.save(khuyenMai);
            // Sử dụng RedirectAttributes để thêm thông báo thành công sau khi redirect
            redirectAttributes.addFlashAttribute("successMessage", "Lưu khuyến mãi thành công!");
            return "redirect:/khuyen-mai"; // Redirect về trang danh sách
        }
    }

    @GetMapping("/sua/{id}")
    public String hienFormSua(@PathVariable("id") Integer id, Model model) {
        Optional<KhuyenMai> optionalKhuyenMai = khuyenMaiRepo.findById(id);
        if (optionalKhuyenMai.isPresent()) {
            model.addAttribute("khuyenMai", optionalKhuyenMai.get());
            return "khuyenMai/form";
        } else {
            // Xử lý trường hợp không tìm thấy, ví dụ: hiển thị trang lỗi hoặc redirect
            // Có thể thêm thông báo lỗi vào RedirectAttributes nếu redirect
            return "redirect:/khuyen-mai"; // Hoặc trang lỗi 404
        }
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        KhuyenMai khuyenMai = khuyenMaiRepo.findById(id).orElse(null);
        if (khuyenMai != null) {
            try { // Nên bọc lại thao tác lưu để bắt lỗi DB nếu có
                khuyenMai.setTrangThai(false); // Set trạng thái to 'Không hoạt động'
                khuyenMaiRepo.save(khuyenMai);
                redirectAttributes.addFlashAttribute("successMessage", "Trạng thái đã được cập nhật thành 'Không hoạt động'!"); // Đổi tên biến
            } catch (Exception e) {
                // Bắt lỗi khi lưu
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật trạng thái: " + e.getMessage()); // Đổi tên biến
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khuyến mãi để cập nhật trạng thái!"); // Đổi tên biến
        }
        return "redirect:/khuyen-mai";
    }

}