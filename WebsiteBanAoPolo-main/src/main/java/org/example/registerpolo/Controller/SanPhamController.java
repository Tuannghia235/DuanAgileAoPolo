package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.SanPham;
import org.example.registerpolo.Entity.ThuongHieu;
import org.example.registerpolo.Repository.SanPhamRepo;
import org.example.registerpolo.Repository.ThuongHieuRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamRepo sanPhamRepo;
    @Autowired
    private ThuongHieuRepo thuongHieuRepo;
    @GetMapping
    public String hienThiTatCaSanPham(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<SanPham> sanPhams;
        if (search != null && !search.isEmpty()) {
            sanPhams = sanPhamRepo.findByTenContainingIgnoreCase(search, PageRequest.of(page, size));
        } else {
            sanPhams = sanPhamRepo.findAll(PageRequest.of(page, size));
        }
        model.addAttribute("sanPhams", sanPhams);
        model.addAttribute("search", search);
        return "san-pham/danh-sach";
    }




    @GetMapping("/them-moi")
    public String hienThiFormThemMoi(Model model) {
        model.addAttribute("sanPham", new SanPham());
        return "san-pham/them-moi";
    }

    @PostMapping("/them-moi")
    public String themMoiSanPham(@ModelAttribute SanPham sanPham, RedirectAttributes redirectAttributes) {
        try {
            // Thiết lập trạng thái mặc định nếu chưa được thiết lập
            if (sanPham.getTrangThai() == null) {
                sanPham.setTrangThai(true);
            }
            sanPhamRepo.save(sanPham);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới sản phẩm thành công!");
            return "redirect:/san-pham";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            return "redirect:/san-pham/them-moi";
        }
    }

    @GetMapping("/chi-tiet/{id}")
    public String xemChiTietSanPham(@PathVariable Integer id, Model model) {
        Optional<SanPham> sanPhamOpt = sanPhamRepo.findById(id);
        if (sanPhamOpt.isPresent()) {
            model.addAttribute("sanPham", sanPhamOpt.get());
            return "san-pham/chi-tiet";
        } else {
            return "redirect:/san-pham";
        }
    }

    @GetMapping("/sua/{id}")
    public String hienThiFormCapNhat(@PathVariable Integer id, Model model) {
        Optional<SanPham> sanPhamOpt = sanPhamRepo.findById(id);
        if (sanPhamOpt.isPresent()) {
            // Sử dụng phương thức findAll() của JpaRepository để lấy tất cả thương hiệu
            model.addAttribute("thuongHieus", thuongHieuRepo.findByTrangThaiTrue());

            model.addAttribute("sanPham", sanPhamOpt.get());
            return "san-pham/cap-nhat";
        } else {
            return "redirect:/san-pham";
        }
    }

    @PostMapping("/sua/{id}")
    public String capNhatSanPham(@PathVariable Integer id,
                                 @ModelAttribute SanPham sanPham,
                                 @RequestParam("thuongHieuId") Integer thuongHieuId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Optional<SanPham> sanPhamOpt = sanPhamRepo.findById(id);
            if (sanPhamOpt.isPresent()) {
                SanPham existingSanPham = sanPhamOpt.get();
                existingSanPham.setMa(sanPham.getMa());
                existingSanPham.setTen(sanPham.getTen());
                existingSanPham.setTrangThai(sanPham.getTrangThai());

                // Set thương hiệu cho sản phẩm
                ThuongHieu thuongHieu = thuongHieuRepo.findById(thuongHieuId).orElse(null);
                existingSanPham.setThuongHieu(thuongHieu);

                sanPhamRepo.save(existingSanPham);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm!");
            }
            return "redirect:/san-pham";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return "redirect:/san-pham/sua/" + id;
        }
    }



    @PostMapping("/xoa/{id}")
    public String xoaSanPham(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<SanPham> sanPhamOpt = sanPhamRepo.findById(id);
        if (sanPhamOpt.isPresent()) {
            SanPham sanPham = sanPhamOpt.get();
            sanPham.setTrangThai(false); // Set trạng thái to 'Không hoạt động'
            sanPhamRepo.save(sanPham);
            redirectAttributes.addFlashAttribute("successMessage", "Trạng thái đã được cập nhật thành 'Không hoạt động'!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm!");
        }
        return "redirect:/san-pham";
    }
} 