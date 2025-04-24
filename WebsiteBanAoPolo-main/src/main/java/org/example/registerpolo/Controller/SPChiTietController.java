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
import java.math.BigDecimal;
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

    // Inject ThuongHieuRepo nếu bạn có entity ThuongHieu riêng
    @Autowired
    private ThuongHieuRepo thuongHieuRepo; // GIẢ ĐỊNH BẠN CÓ REPO NÀY


    // Bổ sung bộ lọc Thương hiệu và truyền danh sách Thương hiệu vào model
    @GetMapping
    public String hienThiTatCaSPChiTiet(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "mauSacId", required = false) Integer mauSacId, // Đổi tên tham số để khớp với form HTML đã sửa
            @RequestParam(value = "kichThuocId", required = false) Integer kichThuocId, // Đổi tên tham số để khớp với form HTML đã sửa
            @RequestParam(value = "thuongHieuId", required = false) Integer thuongHieuId, // THÊM: Tham số lọc Thương hiệu
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        // Cần cập nhật findAllWithFilters trong SPChiTietRepo để nhận thêm thuongHieuId
        Page<SPChiTiet> spChiTiets = spChiTietRepo.findAllWithFilters(search, mauSacId, kichThuocId, thuongHieuId, PageRequest.of(page, size));

        model.addAttribute("spChiTiets", spChiTiets);
        model.addAttribute("search", search);
        model.addAttribute("mauSacId", mauSacId);
        model.addAttribute("kichThuocId", kichThuocId);
        model.addAttribute("thuongHieuId", thuongHieuId); // THÊM: Truyền giá trị lọc hiện tại

        model.addAttribute("mauSacs", mauSacRepo.findAll());
        model.addAttribute("kichThuocs", kichThuocRepo.findAll());
        model.addAttribute("thuongHieus", thuongHieuRepo.findAll()); // THÊM: Truyền danh sách Thương hiệu cho dropdown

        return "sp-chi-tiet/danh-sach";
    }

    @GetMapping("/them-moi")
    public String hienThiFormThemMoi(Model model) {
        model.addAttribute("spChiTiet", new SPChiTiet());
        model.addAttribute("sanPhams", sanPhamRepo.findAll());
        model.addAttribute("mauSacs", mauSacRepo.findAll());
        model.addAttribute("kichThuocs", kichThuocRepo.findAll());
        model.addAttribute("thuongHieus", thuongHieuRepo.findAll()); // Add this line
        return "sp-chi-tiet/them-moi";
    }

    @PostMapping("/them-moi")
    public String themMoiSPChiTiet(@ModelAttribute SPChiTiet spChiTiet,
                                   @RequestParam("fileHinhAnh") MultipartFile[] files,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Các thiết lập default value
            if (spChiTiet.getTrangThai() == null) {
                spChiTiet.setTrangThai(true);
            }
            if (spChiTiet.getSoLuong() == null) {
                spChiTiet.setSoLuong(0);
            }
            if (spChiTiet.getDonGia() == null) { // Thêm kiểm tra donGia nếu cần
                spChiTiet.setDonGia(BigDecimal.ZERO); // Hoặc giá trị default khác
            }

            // Lưu chi tiết sản phẩm trước để có ID
            SPChiTiet savedSPCT = spChiTietRepo.save(spChiTiet); // savedSPCT giờ có ID

            // Xử lý upload hình ảnh nếu có
            if (files != null && files.length > 0) {
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

                        // Lưu thông tin ảnh vào DB và liên kết với SPCT vừa lưu
                        HinhAnhSPChiTiet hinhAnh = new HinhAnhSPChiTiet();
                        hinhAnh.setSpChiTiet(savedSPCT); // Liên kết với savedSPCT
                        hinhAnh.setUrl("/image/" + fileName);
                        hinhAnh.setMoTa("Ảnh sản phẩm " + savedSPCT.getMaSPCT()); // Sử dụng mã từ savedSPCT
                        hinhAnh.setTrangThai(true);
                        hinhAnhRepo.save(hinhAnh);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới chi tiết sản phẩm thành công!");
            return "redirect:/san-pham-chi-tiet";
        } catch (Exception e) {
            e.printStackTrace(); // In stack trace để debug
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm chi tiết sản phẩm: " + e.getMessage());
            // Nếu có lỗi, có thể redirect về trang thêm mới và giữ lại dữ liệu đã nhập nếu dùng RedirectAttributes cho model attribute
            // return "redirect:/san-pham-chi-tiet/them-moi";
            return "sp-chi-tiet/them-moi"; // Hoặc trả về view để hiển thị lỗi và giữ lại form
        }
    }


    @GetMapping("/chi-tiet/{id}")
    public String xemChiTietSPChiTiet(@PathVariable Integer id, Model model) {
        Optional<SPChiTiet> spChiTietOpt = spChiTietRepo.findById(id);
        if (spChiTietOpt.isPresent()) {
            SPChiTiet spChiTiet = spChiTietOpt.get();

            // Lấy danh sách ảnh của sản phẩm chi tiết
            List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhRepo.findBySpChiTiet(spChiTiet); // Hoặc findBySpChiTiet_Id(id)

            // Thêm thông tin sản phẩm chi tiết và danh sách ảnh vào model
            model.addAttribute("spChiTiet", spChiTiet);
            model.addAttribute("hinhAnhList", hinhAnhList);

            return "sp-chi-tiet/chi-tiet";
        } else {
            // Nên thêm thông báo lỗi nếu không tìm thấy
            // redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sản phẩm!");
            return "redirect:/san-pham-chi-tiet";
        }
    }


    @GetMapping("/sua/{id}")
    public String hienThiFormCapNhat(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<SPChiTiet> spChiTietOpt = spChiTietRepo.findById(id);
        if (spChiTietOpt.isPresent()) {
            SPChiTiet spChiTiet = spChiTietOpt.get();
            model.addAttribute("spChiTiet", spChiTiet);

            // Load dropdowns
            model.addAttribute("sanPhams", sanPhamRepo.findAll());
            model.addAttribute("mauSacs", mauSacRepo.findAll());
            model.addAttribute("kichThuocs", kichThuocRepo.findAll());
            model.addAttribute("thuongHieus", thuongHieuRepo.findAll()); // THÊM: Truyền danh sách Thương hiệu cho dropdown

            // Load danh sách ảnh hiện có
            List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhRepo.findBySpChiTiet_Id(id);
            model.addAttribute("hinhAnhList", hinhAnhList);

            return "sp-chi-tiet/cap-nhat";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sản phẩm để sửa!");
            return "redirect:/san-pham-chi-tiet";
        }
    }

    // Phương thức xóa file - Có thể cần sửa đường dẫn cho production nếu nó được gọi ở đây
    // Hiện tại phương thức xóa trong /xoa/{id} đã dùng ClassPathResource nên có thể không cần sửa helper này
    private void xoaFile(String filePath) {
        try {
            // Sử dụng ClassPathResource để tìm đường dẫn tuyệt đối
            String staticPath = new ClassPathResource("static").getFile().getAbsolutePath();
            Path path = Paths.get(staticPath + filePath); // filePath ví dụ: /image/abc.png
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace(); // In lỗi khi không xóa được file
        }
    }

    // Sửa lỗi logic lưu SPCT và liên kết ảnh
    @PostMapping("/sua/{id}")
    public String capNhatSanPhamChiTiet(
            @PathVariable("id") Integer id,
            @ModelAttribute("spChiTiet") SPChiTiet spChiTiet, // Đối tượng nhận dữ liệu từ form
            @RequestParam(name = "files", required = false) MultipartFile[] files, // Ảnh mới upload
            @RequestParam(name = "xoaAnhIds", required = false) List<Integer> xoaAnhIds, // IDs ảnh cũ muốn xóa
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Lấy bản ghi HIỆN TẠI từ DB
            SPChiTiet spChiTietDB = spChiTietRepo.findById(id).orElse(null);
            if (spChiTietDB == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm chi tiết để cập nhật!");
                return "redirect:/san-pham-chi-tiet";
            }

            // 2. Cập nhật thông tin cơ bản từ đối tượng form attribute (spChiTiet) sang đối tượng DB (spChiTietDB)
            spChiTietDB.setMaSPCT(spChiTiet.getMaSPCT());
            spChiTietDB.setSanPham(spChiTiet.getSanPham()); // Cần đảm bảo form gửi về đúng đối tượng SanPham/ID
            spChiTietDB.setMauSac(spChiTiet.getMauSac()); // Cần đảm bảo form gửi về đúng đối tượng MauSac/ID
            spChiTietDB.setKichThuoc(spChiTiet.getKichThuoc()); // Cần đảm bảo form gửi về đúng đối tượng KichThuoc/ID
            spChiTietDB.setSoLuong(spChiTiet.getSoLuong());
            spChiTietDB.setDonGia(spChiTiet.getDonGia());
            spChiTietDB.setTrangThai(spChiTiet.getTrangThai());

            // 3. Xóa ảnh cũ nếu có chọn
            if (xoaAnhIds != null && !xoaAnhIds.isEmpty()) {
                List<HinhAnhSPChiTiet> anhCanXoa = hinhAnhRepo.findAllById(xoaAnhIds);
                for (HinhAnhSPChiTiet anh : anhCanXoa) {
                    // Xóa file vật lý
                    xoaFile(anh.getUrl()); // Sử dụng helper method đã sửa (hoặc code trực tiếp ClassPathResource)
                    hinhAnhRepo.delete(anh); // Xóa khỏi DB
                }
            }

            // 4. Lưu ảnh mới nếu có
            if (files != null && files.length > 0) {
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

                        // Lưu thông tin ảnh vào DB và liên kết với SPCT TỪ DB (spChiTietDB)
                        HinhAnhSPChiTiet hinhAnh = new HinhAnhSPChiTiet();
                        hinhAnh.setSpChiTiet(spChiTietDB); // Liên kết với đối tượng SPCT từ DB
                        hinhAnh.setUrl("/image/" + fileName);
                        hinhAnh.setMoTa("Ảnh sản phẩm " + spChiTietDB.getMaSPCT()); // Sử dụng mã từ spChiTietDB
                        hinhAnh.setTrangThai(true);
                        hinhAnhRepo.save(hinhAnh);
                    }
                }
            }

            // 5. Lưu thay đổi vào DB (lưu đối tượng spChiTietDB đã được cập nhật)
            spChiTietRepo.save(spChiTietDB); // Lưu đối tượng từ DB sau khi đã cập nhật thông tin và xử lý ảnh

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chi tiết sản phẩm thành công!");
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi để debug
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật chi tiết sản phẩm: " + e.getMessage());
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

                // 2. Xóa ảnh trong thư mục vật lý
                String staticImagePath = new ClassPathResource("static/image").getFile().getAbsolutePath();
                for (HinhAnhSPChiTiet anh : hinhAnhList) {
                    if (anh.getUrl() != null) {
                        String fileName = anh.getUrl().replace("/image/", ""); // lấy tên file
                        File file = new File(staticImagePath + File.separator + fileName);
                        if (file.exists()) {
                            boolean deleted = file.delete();
                            if (!deleted) {
                                System.err.println("Failed to delete file: " + file.getAbsolutePath());
                                // Có thể log hoặc xử lý lỗi xóa file ở đây
                            }
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
            e.printStackTrace(); // In lỗi để debug
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa chi tiết sản phẩm: " + e.getMessage());
        }
        return "redirect:/san-pham-chi-tiet";
    }

}