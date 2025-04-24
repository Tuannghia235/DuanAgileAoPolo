package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.KichThuoc;

import org.example.registerpolo.Repository.KichThuocRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    @ResponseBody
    public Map<String, Object> saveKichThuoc(@ModelAttribute KichThuoc kichThuoc) {
        Map<String, Object> response = new HashMap<>();
        try {
            KichThuoc savedKichThuoc = kichThuocRepository.save(kichThuoc);
            response.put("success", true);
            response.put("id", savedKichThuoc.getId());
            response.put("ten", savedKichThuoc.getTen());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/sua/{id}")
    public String sua(@PathVariable Integer id, Model model) {
        KichThuoc kt = kichThuocRepository.findById(id).orElse(null);
        if (kt == null) return "redirect:/kich-thuoc";
        model.addAttribute("kichThuoc", kt);
        return "kich-thuoc/form";
    }

    @GetMapping("/xoa/{id}")
    public String xoa(@PathVariable Integer id) {
        kichThuocRepository.deleteById(id);
        return "redirect:/kich-thuoc?success=Đã xóa";
    }
}