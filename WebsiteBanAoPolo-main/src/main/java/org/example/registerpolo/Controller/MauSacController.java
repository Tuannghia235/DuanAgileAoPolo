package org.example.registerpolo.Controller;


import org.example.registerpolo.Entity.MauSac;

import org.example.registerpolo.Repository.MauSacRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/mau-sac")
public class MauSacController {

    @Autowired
    private MauSacRepo repo;

    @GetMapping
    public String hienThiDanhSach(Model model) {
        model.addAttribute("danhSachMau", repo.findAll());
        return "mau-sac/index";
    }

    @GetMapping("/them")
    public String hienThiFormThem(Model model) {
        model.addAttribute("mauSac", new MauSac());
        return "mau-sac/form";
    }
    @PostMapping("/save")
    public String luuMauSac(@ModelAttribute MauSac mauSac, RedirectAttributes redirectAttributes) {
        // Check if it's an update operation
        if (mauSac.getId() != null) {
            MauSac existingMauSac = repo.findById(mauSac.getId()).orElse(null);
            if (existingMauSac != null && !existingMauSac.getMa().equals(mauSac.getMa())) {
                redirectAttributes.addFlashAttribute("error", "Không thể thay đổi mã!");
                return "redirect:/mau-sac/sua/" + mauSac.getId();
            }
        } else {
            // Validate uniqueness of 'ma' for new entries
            if (repo.existsByMa(mauSac.getMa())) {
                redirectAttributes.addFlashAttribute("error", "Mã đã tồn tại!");
                return "redirect:/mau-sac/them";
            }
        }

        repo.save(mauSac);
        redirectAttributes.addFlashAttribute("success", "Lưu thành công!");
        return "redirect:/mau-sac";
    }
    @PostMapping("/save-ajax")
    @ResponseBody
    public Map<String, Object> saveMauSac(@ModelAttribute MauSac mauSac) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate uniqueness of 'ma'
            if (mauSac.getId() == null && repo.existsByMa(mauSac.getMa())) {
                response.put("success", false);
                response.put("field", "ma");
                response.put("message", "Mã đã tồn tại!");
                return response;
            }
            if (mauSac.getId() == null) { // Kiểm tra nếu là entity mới (chưa có ID)
                mauSac.setTrangThai(true); // Đặt trạng thái mặc định là hoạt động
            }

            MauSac savedMauSac = repo.save(mauSac);
            response.put("success", true);
            response.put("id", savedMauSac.getId());
            response.put("ma", savedMauSac.getMa());
            response.put("ten", savedMauSac.getTen());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/sua/{id}")
    public String hienThiFormSua(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        MauSac mauSac = repo.findById(id).orElse(null);
        if (mauSac == null) return "redirect:/mau-sac";
        model.addAttribute("mauSac", mauSac);
        redirectAttributes.addFlashAttribute("success", "Sửa thành công!");
        return "mau-sac/form";
    }

    @PostMapping("/xoa/{id}")
    public String xoaMauSac(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        MauSac mauSac = repo.findById(id).orElse(null);
        if (mauSac != null) {
            mauSac.setTrangThai(false); // Set trạng thái to 'Không hoạt động'
            repo.save(mauSac);
            redirectAttributes.addFlashAttribute("success", "Trạng thái đã được cập nhật thành 'Không hoạt động'!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy màu sắc!");
        }
        return "redirect:/mau-sac";
    }
}

