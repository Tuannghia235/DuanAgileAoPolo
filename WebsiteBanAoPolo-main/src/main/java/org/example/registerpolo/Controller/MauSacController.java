package org.example.registerpolo.Controller;


import org.example.registerpolo.Entity.MauSac;

import org.example.registerpolo.Repository.MauSacRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

