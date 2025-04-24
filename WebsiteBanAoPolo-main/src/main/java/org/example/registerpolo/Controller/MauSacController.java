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
        repo.save(mauSac);
        redirectAttributes.addFlashAttribute("success", "Lưu thành công!");
        return "redirect:/mau-sac";
    }
    @PostMapping("/save-ajax")
    @ResponseBody
    public Map<String, Object> saveMauSac(@ModelAttribute MauSac mauSac) {
        Map<String, Object> response = new HashMap<>();
        try {
            MauSac savedMauSac = repo.save(mauSac);
            response.put("success", true);
            response.put("id", savedMauSac.getId());
            response.put("ten", savedMauSac.getTen());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/sua/{id}")
    public String hienThiFormSua(@PathVariable Integer id, Model model) {
        MauSac mauSac = repo.findById(id).orElse(null);
        if (mauSac == null) return "redirect:/mau-sac";
        model.addAttribute("mauSac", mauSac);
        return "mau-sac/form";
    }

    @GetMapping("/xoa/{id}")
    public String xoaMauSac(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        repo.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        return "redirect:/mau-sac";
    }
}

