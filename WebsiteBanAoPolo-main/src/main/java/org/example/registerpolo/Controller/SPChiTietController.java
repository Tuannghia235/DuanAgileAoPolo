package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.*;
import org.example.registerpolo.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/san-pham-chi-tiet")
public class SPChiTietController {

    @Autowired
    private SPChiTietRepo spChiTietRepo;

    @Autowired
    private SanPhamRepo sanPhamRepo;

    @Autowired
    private MauSacRepo mauSacRepo;

    @Autowired
    private KichThuocRepo kichThuocRepo;
    @Autowired
    private HinhAnhSPChiTietRepo hinhAnhRepo;

    private final String uploadDir = "src/main/resources/static/image";// thư mục chứa ảnh

    @GetMapping
    public String hienThiTatCaSPChiTiet(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "mauSac", required = false) Integer mauSacId,
            @RequestParam(value = "kichThuoc", required = false) Integer kichThuocId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<SPChiTiet> spChiTiets = spChiTietRepo.findAllWithFilters(search, mauSacId, kichThuocId, PageRequest.of(page, size));
        model.addAttribute("spChiTiets", spChiTiets);
        model.addAttribute("search", search);
        model.addAttribute("mauSacId", mauSacId);
        model.addAttribute("kichThuocId", kichThuocId);
        model.addAttribute("mauSacs", mauSacRepo.findAll());
        model.addAttribute("kichThuocs", kichThuocRepo.findAll());
        return "sp-chi-tiet/danh-sach";
    }

    @GetMapping("/them-moi")
    public String hienThiFormThemMoi(Model model) {
        model.addAttribute("spChiTiet", new SPChiTiet());
        model.addAttribute("sanPhams", sanPhamRepo.findAll());
        model.addAttribute("mauSacs", mauSacRepo.findAll());
        model.addAttribute("kichThuocs", kichThuocRepo.findAll());
        return "sp-chi-tiet/them-moi";
    }

    @PostMapping("/them-moi")
    public String themMoiSPChiTiet(@ModelAttribute SPChiTiet spChiTiet,
                                   @RequestParam("fileHinhAnh") MultipartFile[] files,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (spChiTiet.getTrangThai() == null) {
                spChiTiet.setTrangThai(true);
            }
            if (spChiTiet.getSoLuong() == null) {
                spChiTiet.setSoLuong(0);
            }

            // Lưu chi tiết sản phẩm
            SPChiTiet savedSPCT = spChiTietRepo.save(spChiTiet);

            // Xử lý upload hình ảnh nếu có
            if (files != null && files.length > 0) {
                // Đảm bảo thư mục tồn tại
                String staticImagePath = new ClassPathResource("static/image").getFile().getAbsolutePath();
                File dir = new File(staticImagePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        Path uploadPath = Paths.get(staticImagePath, fileName);
                        file.transferTo(uploadPath.toFile());

                        // Lưu thông tin ảnh vào DB
                        HinhAnhSPChiTiet hinhAnh = new HinhAnhSPChiTiet();
                        hinhAnh.setSpChiTiet(savedSPCT);
                        hinhAnh.setUrl("/image/" + fileName);
                        hinhAnh.setMoTa("Ảnh sản phẩm " + savedSPCT.getMaSPCT());
                        hinhAnh.setTrangThai(true);
                        hinhAnhRepo.save(hinhAnh);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới chi tiết sản phẩm thành công!");
            return "redirect:/san-pham-chi-tiet";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm chi tiết sản phẩm: " + e.getMessage());
            return "redirect:/san-pham-chi-tiet/them-moi";
        }
    }


    @GetMapping("/chi-tiet/{id}")
    public String xemChiTietSPChiTiet(@PathVariable Integer id, Model model) {
        Optional<SPChiTiet> spChiTietOpt = spChiTietRepo.findById(id);
        if (spChiTietOpt.isPresent()) {
            SPChiTiet spChiTiet = spChiTietOpt.get();

            // Lấy danh sách ảnh của sản phẩm chi tiết
            List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhRepo.findBySpChiTiet(spChiTiet);

            // Thêm thông tin sản phẩm chi tiết và danh sách ảnh vào model
            model.addAttribute("spChiTiet", spChiTiet);
            model.addAttribute("hinhAnhList", hinhAnhList);

            return "sp-chi-tiet/chi-tiet";
        } else {
            return "redirect:/san-pham-chi-tiet";
        }
    }


    @GetMapping("/sua/{id}")
    public String hienThiFormCapNhat(@PathVariable Integer id, Model model) {
        Optional<SPChiTiet> spChiTietOpt = spChiTietRepo.findById(id);
        if (spChiTietOpt.isPresent()) {
            SPChiTiet spChiTiet = spChiTietOpt.get();
            model.addAttribute("spChiTiet", spChiTiet);

            // Load dropdowns
            model.addAttribute("sanPhams", sanPhamRepo.findAll());
            model.addAttribute("mauSacs", mauSacRepo.findAll());
            model.addAttribute("kichThuocs", kichThuocRepo.findAll());

            // Load danh sách ảnh hiện có
            List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhRepo.findBySpChiTiet_Id(id);
            model.addAttribute("hinhAnhList", hinhAnhList);

            return "sp-chi-tiet/cap-nhat";
        } else {
            return "redirect:/san-pham-chi-tiet";
        }
    }
    private void xoaFile(String filePath) {
        try {
            Path path = Paths.get("src/main/resources/static" + filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/sua/{id}")
    public String capNhatSanPhamChiTiet(
            @PathVariable("id") Integer id,
            @ModelAttribute("spChiTiet") SPChiTiet spChiTiet,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(name = "xoaAnhIds", required = false) List<Integer> xoaAnhIds,
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Lấy bản ghi hiện tại từ DB
            SPChiTiet spChiTietDB = spChiTietRepo.findById(id).orElse(null);
            if (spChiTietDB == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm chi tiết!");
                return "redirect:/san-pham-chi-tiet";
            }

            // 2. Cập nhật thông tin cơ bản
            spChiTietDB.setMaSPCT(spChiTiet.getMaSPCT());
            spChiTietDB.setSanPham(spChiTiet.getSanPham());
            spChiTietDB.setMauSac(spChiTiet.getMauSac());
            spChiTietDB.setKichThuoc(spChiTiet.getKichThuoc());
            spChiTietDB.setSoLuong(spChiTiet.getSoLuong());
            spChiTietDB.setDonGia(spChiTiet.getDonGia());
            spChiTietDB.setTrangThai(spChiTiet.getTrangThai());

            // 3. Xóa ảnh cũ nếu có chọn
            if (xoaAnhIds != null) {
                List<HinhAnhSPChiTiet> anhCanXoa = hinhAnhRepo.findAllById(xoaAnhIds);
                for (HinhAnhSPChiTiet anh : anhCanXoa) {
                    // Xóa file vật lý nếu cần
                    xoaFile(anh.getUrl());
                    hinhAnhRepo.delete(anh);
                }
            }
// Lưu chi tiết sản phẩm
            SPChiTiet savedSPCT = spChiTietRepo.save(spChiTiet);
            // 4. Lưu ảnh mới nếu có
            if (files != null && files.length > 0) {
                // Đảm bảo thư mục tồn tại
                String staticImagePath = new ClassPathResource("static/image").getFile().getAbsolutePath();
                File dir = new File(staticImagePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        Path uploadPath = Paths.get(staticImagePath, fileName);
                        file.transferTo(uploadPath.toFile());

                        // Lưu thông tin ảnh vào DB
                        HinhAnhSPChiTiet hinhAnh = new HinhAnhSPChiTiet();
                        hinhAnh.setSpChiTiet(savedSPCT);
                        hinhAnh.setUrl("/image/" + fileName);
                        hinhAnh.setMoTa("Ảnh sản phẩm " + savedSPCT.getMaSPCT());
                        hinhAnh.setTrangThai(true);
                        hinhAnhRepo.save(hinhAnh);
                    }
                }
            }

            // 5. Lưu thay đổi sản phẩm chi tiết
            spChiTietRepo.save(spChiTietDB);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật!");
        }

        return "redirect:/san-pham-chi-tiet";
    }

    @GetMapping("/xoa/{id}")
    public String xoaSPChiTiet(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Optional<SPChiTiet> spChiTietOpt = spChiTietRepo.findById(id);
            if (spChiTietOpt.isPresent()) {
                SPChiTiet spChiTiet = spChiTietOpt.get();

                // 1. Lấy danh sách hình ảnh liên quan
                List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhRepo.findBySpChiTiet_Id(id);

                // 2. Xóa ảnh trong thư mục
                for (HinhAnhSPChiTiet anh : hinhAnhList) {
                    if (anh.getUrl() != null) {
                        String fileName = anh.getUrl().replace("/image/", ""); // lấy tên file
                        String staticImagePath = new ClassPathResource("static/image").getFile().getAbsolutePath();
                        File file = new File(staticImagePath + File.separator + fileName);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }

                // 3. Xóa dữ liệu ảnh trong DB
                hinhAnhRepo.deleteAll(hinhAnhList);

                // 4. Xóa chi tiết sản phẩm
                spChiTietRepo.deleteById(id);

                redirectAttributes.addFlashAttribute("successMessage", "Xóa chi tiết sản phẩm và hình ảnh thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sản phẩm!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa chi tiết sản phẩm: " + e.getMessage());
        }
        return "redirect:/san-pham-chi-tiet";
    }

} 