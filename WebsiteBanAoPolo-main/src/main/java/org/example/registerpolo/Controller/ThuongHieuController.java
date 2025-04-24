package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.KichThuoc;
import org.example.registerpolo.Entity.ThuongHieu;
import org.example.registerpolo.Repository.ThuongHieuRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/thuong-hieu")
public class ThuongHieuController {

    @Autowired
    private ThuongHieuRepo thuongHieuRepository;

    @GetMapping
    public String getAllThuongHieu(Model model) {
        List<ThuongHieu> thuongHieuList = thuongHieuRepository.findAll();
        model.addAttribute("thuongHieuList", thuongHieuList);
        return "thuong-hieu/danh-sach";
    }

    @GetMapping("/them")
    public String createThuongHieuForm(Model model) {
        model.addAttribute("thuongHieu", new ThuongHieu());
        return "thuong-hieu/them";
    }

    @PostMapping("/save")
    public String createThuongHieu(@ModelAttribute ThuongHieu thuongHieu, RedirectAttributes redirectAttributes) {
        // Check if it's an update operation
        if (thuongHieu.getId() != null) {
            ThuongHieu existingThuongHieu = thuongHieuRepository.findById(thuongHieu.getId()).orElse(null);
            if (existingThuongHieu != null && !existingThuongHieu.getTen().equals(thuongHieu.getTen())) {
                redirectAttributes.addFlashAttribute("error", "Không thể thay đổi tên thương hiệu!");
                return "redirect:/thuong-hieu/sua/" + thuongHieu.getId();
            }
        } else {
            // Validate uniqueness of 'ten' for new entries
            if (thuongHieuRepository.existsByTen(thuongHieu.getTen())) {
                redirectAttributes.addFlashAttribute("error", "Tên thương hiệu đã tồn tại!");
                return "redirect:/thuong-hieu/them";
            }
        }

        thuongHieuRepository.save(thuongHieu);
        redirectAttributes.addFlashAttribute("success", "Lưu thành công!");
        return "redirect:/thuong-hieu";
    }

    @PostMapping("/save-ajax")
    @ResponseBody
    public Map<String, Object> saveThuongHieu(@ModelAttribute ThuongHieu thuongHieu) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate uniqueness of 'ten'
            if (thuongHieu.getId() == null && thuongHieuRepository.existsByTen(thuongHieu.getTen())) {
                response.put("success", false);
                response.put("field", "ten");
                response.put("message", "Tên thương hiệu đã tồn tại!");
                return response;
            }
            if (thuongHieu.getId() == null) { // Kiểm tra nếu là entity mới (chưa có ID)
                thuongHieu.setTrangThai(true); // Đặt trạng thái mặc định là hoạt động
            }

            ThuongHieu savedThuongHieu = thuongHieuRepository.save(thuongHieu);
            response.put("success", true);
            response.put("id", savedThuongHieu.getId());
            response.put("ten", savedThuongHieu.getTen());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/sua/{id}")
    public String editThuongHieuForm(@PathVariable Integer id, Model model) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id).orElse(null);
        model.addAttribute("thuongHieu", thuongHieu);
        return "thuong-hieu/sua";
    }

    @PostMapping("/sua/{id}")
    public String updateThuongHieu(@PathVariable Integer id, @ModelAttribute ThuongHieu thuongHieu, RedirectAttributes redirectAttributes) {
        thuongHieu.setId(id);
        thuongHieuRepository.save(thuongHieu);
        redirectAttributes.addFlashAttribute("success", "Sửa thành công!");
        return "redirect:/thuong-hieu";
    }

    @PostMapping("/xoa/{id}")
    public String xoaThuongHieu(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id).orElse(null);
        if (thuongHieu != null) {
            thuongHieu.setTrangThai(false); // Set trạng thái to 'Không hoạt động'
            thuongHieuRepository.save(thuongHieu);
            redirectAttributes.addFlashAttribute("success", "Trạng thái đã được cập nhật thành 'Không hoạt động'!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thương hiệu!");
        }
        return "redirect:/thuong-hieu";
    }
}