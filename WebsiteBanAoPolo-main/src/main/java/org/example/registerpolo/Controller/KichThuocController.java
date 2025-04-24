package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.KichThuoc;

import org.example.registerpolo.Repository.KichThuocRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/kich-thuoc")
public class KichThuocController {

    @Autowired
    private KichThuocRepo kichThuocRepository;

    @GetMapping
    public String hienThi(Model model, @RequestParam(value = "success", required = false) String success) {
        model.addAttribute("danhSach", kichThuocRepository.findAll());
        if (success != null) model.addAttribute("success", success);
        return "kich-thuoc/index";
    }

    @GetMapping("/them")
    public String formThem(Model model) {
        model.addAttribute("kichThuoc", new KichThuoc());
        return "kich-thuoc/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute KichThuoc kichThuoc, RedirectAttributes redirectAttributes) {
        // Check if it's an update operation
        if (kichThuoc.getId() != null) {
            KichThuoc existingKichThuoc = kichThuocRepository.findById(kichThuoc.getId()).orElse(null);
            if (existingKichThuoc != null && !existingKichThuoc.getMa().equals(kichThuoc.getMa())) {
                redirectAttributes.addFlashAttribute("error", "Không thể thay đổi mã!");
                return "redirect:/kich-thuoc/sua/" + kichThuoc.getId();
            }
        } else {
            // Validate uniqueness of 'ma' for new entries
            if (kichThuocRepository.existsByMa(kichThuoc.getMa())) {
                redirectAttributes.addFlashAttribute("error", "Mã đã tồn tại!");
                return "redirect:/kich-thuoc/them";
            }
        }

        kichThuocRepository.save(kichThuoc);
        redirectAttributes.addFlashAttribute("success", "Lưu thành công!");
        return "redirect:/kich-thuoc";
    }
    @PostMapping("/save-ajax")
    @ResponseBody
    public Map<String, Object> saveKichThuoc(@ModelAttribute KichThuoc kichThuoc) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate uniqueness of 'ma'
            if (kichThuoc.getId() == null && kichThuocRepository.existsByMa(kichThuoc.getMa())) {
                response.put("success", false);
                response.put("field", "ma");
                response.put("message", "Mã kích thước đã tồn tại!");
                return response;
            }
            if (kichThuoc.getId() == null) { // Kiểm tra nếu là entity mới (chưa có ID)
                kichThuoc.setTrangThai(true); // Đặt trạng thái mặc định là hoạt động
            }

            KichThuoc savedKichThuoc = kichThuocRepository.save(kichThuoc);
            response.put("success", true);
            response.put("id", savedKichThuoc.getId());
            response.put("ma", savedKichThuoc.getMa());
            response.put("ten", savedKichThuoc.getTen());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/sua/{id}")
    public String sua(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        KichThuoc kt = kichThuocRepository.findById(id).orElse(null);
        if (kt == null) return "redirect:/kich-thuoc";
        model.addAttribute("kichThuoc", kt);
        redirectAttributes.addFlashAttribute("success", "Sửa thành công!");
        return "kich-thuoc/form";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        KichThuoc kichThuoc = kichThuocRepository.findById(id).orElse(null);
        if (kichThuoc != null) {
            kichThuoc.setTrangThai(false); // Set trạng thái to 'Không hoạt động'
            kichThuocRepository.save(kichThuoc);
            redirectAttributes.addFlashAttribute("success", "Trạng thái đã được cập nhật thành 'Không hoạt động'!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy kích thước!");
        }
        return "redirect:/kich-thuoc";
    }
}