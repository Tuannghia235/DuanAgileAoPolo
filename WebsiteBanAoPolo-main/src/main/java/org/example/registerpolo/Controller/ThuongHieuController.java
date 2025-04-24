package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.ThuongHieu;
import org.example.registerpolo.Repository.ThuongHieuRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/them")
    public String createThuongHieu(@ModelAttribute ThuongHieu thuongHieu) {
        thuongHieuRepository.save(thuongHieu);
        return "redirect:/thuong-hieu";
    }

    @GetMapping("/sua/{id}")
    public String editThuongHieuForm(@PathVariable Integer id, Model model) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id).orElse(null);
        model.addAttribute("thuongHieu", thuongHieu);
        return "thuong-hieu/sua";
    }

    @PostMapping("/sua/{id}")
    public String updateThuongHieu(@PathVariable Integer id, @ModelAttribute ThuongHieu thuongHieu) {
        thuongHieu.setId(id);
        thuongHieuRepository.save(thuongHieu);
        return "redirect:/thuong-hieu";
    }

    @GetMapping("/xoa/{id}")
    public String deleteThuongHieu(@PathVariable Integer id) {
        thuongHieuRepository.deleteById(id);
        return "redirect:/thuong-hieu";
    }
}