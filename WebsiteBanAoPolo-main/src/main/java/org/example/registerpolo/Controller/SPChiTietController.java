package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.*;
import org.example.registerpolo.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Thêm import này
import org.springframework.core.io.ClassPathResource; // Giữ lại nếu chưa đổi, nhưng xem xét thay thế
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Import để kiểm tra lỗi validation nếu dùng @Valid
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid; // Import nếu dùng validation annotation trên @ModelAttribute

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Autowired
    private ThuongHieuRepo thuongHieuRepo;


    // Helper method để load dropdowns (tránh lặp code)
    private void loadDropdownData(Model model) {
        model.addAttribute("sanPhams", sanPhamRepo.findByTrangThaiTrue());
        model.addAttribute("mauSacs", mauSacRepo.findByTrangThaiTrue());
        model.addAttribute("kichThuocs", kichThuocRepo.findByTrangThaiTrue());
        model.addAttribute("thuongHieus", thuongHieuRepo.findByTrangThaiTrue());
    }

    @GetMapping
    public String hienThiTatCaSPChiTiet(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "mauSacId", required = false) Integer mauSacId,
            @RequestParam(value = "kichThuocId", required = false) Integer kichThuocId,
            @RequestParam(value = "thuongHieuId", required = false) Integer thuongHieuId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Page<SPChiTiet> spChiTiets = spChiTietRepo.findAllWithFilters(search, mauSacId, kichThuocId, thuongHieuId, PageRequest.of(page, size));

        model.addAttribute("spChiTiets", spChiTiets);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", spChiTiets.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("mauSacId", mauSacId);
        model.addAttribute("kichThuocId", kichThuocId);
        model.addAttribute("thuongHieuId", thuongHieuId);

        // Load dropdowns cho bộ lọc
        model.addAttribute("mauSacs", mauSacRepo.findByTrangThaiTrue());
        model.addAttribute("kichThuocs", kichThuocRepo.findByTrangThaiTrue());
        model.addAttribute("thuongHieus", thuongHieuRepo.findByTrangThaiTrue());

        return "sp-chi-tiet/danh-sach";
    }

    @GetMapping("/them-moi")
    public String hienThiFormThemMoi(Model model) {
        model.addAttribute("spChiTiet", new SPChiTiet());
        loadDropdownData(model); // Gọi helper method
        return "sp-chi-tiet/them-moi";
    }

    @PostMapping("/them-moi")
    public String themMoiSPChiTiet(@Valid @ModelAttribute("spChiTiet") SPChiTiet spChiTiet, // Thêm @Valid nếu có validation
                                   BindingResult bindingResult, // Nhận kết quả validation
                                   @RequestParam("fileHinhAnh") MultipartFile[] files,
                                   RedirectAttributes redirectAttributes,
                                   Model model) { // Thêm Model để trả về view khi lỗi

        // Kiểm tra lỗi validation cơ bản (nếu dùng @Valid)
        if (bindingResult.hasErrors()) {
            loadDropdownData(model); // Load lại dropdowns
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập liệu!");
            // spChiTiet đã có sẵn trong model do @ModelAttribute
            return "sp-chi-tiet/them-moi"; // Trả về form với lỗi
        }

        try {
            // Kiểm tra trùng mã SPCT nếu cần
            if (spChiTietRepo.existsByMaSPCT(spChiTiet.getMaSPCT())) {
                loadDropdownData(model);
                model.addAttribute("spChiTiet", spChiTiet); // Giữ lại dữ liệu đã nhập
                model.addAttribute("errorMessage", "Mã chi tiết sản phẩm đã tồn tại!");
                return "sp-chi-tiet/them-moi";
            }


            // Các thiết lập default value
            if (spChiTiet.getTrangThai() == null) {
                spChiTiet.setTrangThai(true);
            }
            if (spChiTiet.getSoLuong() == null) {
                spChiTiet.setSoLuong(0);
            }
            if (spChiTiet.getDonGia() == null) {
                spChiTiet.setDonGia(BigDecimal.ZERO);
            }

            // Lưu chi tiết sản phẩm trước để có ID
            SPChiTiet savedSPCT = spChiTietRepo.save(spChiTiet);

            // Xử lý upload hình ảnh nếu có
            if (files != null && files.length > 0 && !files[0].isEmpty()) {
                // *** CẢNH BÁO: ClassPathResource không đáng tin cậy để GHI file khi deploy JAR/WAR ***
                // Nên dùng đường dẫn cấu hình bên ngoài (ví dụ: biến 'uploadDir' đã inject)
                // Path uploadDirPath = Paths.get(uploadDir); // Sử dụng đường dẫn cấu hình
                Path uploadDirPath = Paths.get(new ClassPathResource("static/image").getURI()); // Tạm dùng ClasspathResource nhưng cần lưu ý
                if (!Files.exists(uploadDirPath)) {
                    Files.createDirectories(uploadDirPath);
                }

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String originalFileName = file.getOriginalFilename();
                        String fileExtension = "";
                        if (originalFileName != null && originalFileName.contains(".")) {
                            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                        }
                        String uniqueFileName = UUID.randomUUID() + fileExtension;
                        Path filePath = uploadDirPath.resolve(uniqueFileName);

                        try (InputStream inputStream = file.getInputStream()) {
                            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                        }

                        // Lưu thông tin ảnh vào DB
                        HinhAnhSPChiTiet hinhAnh = new HinhAnhSPChiTiet();
                        hinhAnh.setSpChiTiet(savedSPCT);
                        hinhAnh.setUrl("/image/" + uniqueFileName); // Đường dẫn tương đối để hiển thị
                        hinhAnh.setMoTa("Ảnh sản phẩm " + savedSPCT.getMaSPCT());
                        hinhAnh.setTrangThai(true);
                        hinhAnhRepo.save(hinhAnh);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới chi tiết sản phẩm thành công!");
            return "redirect:/san-pham-chi-tiet";

        } catch (Exception e) {
            e.printStackTrace(); // Quan trọng: In lỗi ra console để debug
            loadDropdownData(model); // Load lại dropdowns khi có lỗi khác
            model.addAttribute("spChiTiet", spChiTiet); // Giữ lại thông tin người dùng nhập
            model.addAttribute("errorMessage", "Lỗi khi thêm chi tiết sản phẩm: " + e.getMessage());
            return "sp-chi-tiet/them-moi"; // Trả về view thay vì redirect
        }
    }


    @GetMapping("/chi-tiet/{id}")
    public String xemChiTietSPChiTiet(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<SPChiTiet> spChiTietOpt = spChiTietRepo.findById(id);
        if (spChiTietOpt.isPresent()) {
            SPChiTiet spChiTiet = spChiTietOpt.get();
            List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhRepo.findBySpChiTiet_Id(spChiTiet.getId()); // Lấy ảnh theo ID SPCT

            model.addAttribute("spChiTiet", spChiTiet);
            model.addAttribute("hinhAnhList", hinhAnhList);
            return "sp-chi-tiet/chi-tiet";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sản phẩm!");
            return "redirect:/san-pham-chi-tiet";
        }
    }

    @GetMapping("/sua/{id}")
    public String hienThiFormCapNhat(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<SPChiTiet> spChiTietOpt = spChiTietRepo.findById(id);
        if (spChiTietOpt.isPresent()) {
            SPChiTiet spChiTiet = spChiTietOpt.get();
            model.addAttribute("spChiTiet", spChiTiet);
            loadDropdownData(model); // Load dropdowns

            // Load danh sách ảnh hiện có
            List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhRepo.findBySpChiTiet_Id(id);
            model.addAttribute("hinhAnhList", hinhAnhList);

            return "sp-chi-tiet/cap-nhat";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sản phẩm để sửa!");
            return "redirect:/san-pham-chi-tiet";
        }
    }

    // Phương thức xóa file - Cần cẩn thận với đường dẫn khi deploy
    private void xoaFile(String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty()) {
            return;
        }
        try {
            // *** CẢNH BÁO: ClassPathResource không đáng tin cậy để tìm file GHI khi deploy JAR/WAR ***
            // Nên dùng đường dẫn cấu hình bên ngoài (ví dụ: biến 'uploadDir')
            // Path filePath = Paths.get(uploadDir, relativeUrl.replace("/image/", "")); // Nếu dùng uploadDir
            Path filePath = Paths.get(new ClassPathResource("static").getURI()).resolve(relativeUrl.substring(1)); // Tạm dùng ClasspathResource
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Không thể xóa file: " + relativeUrl + " - Lỗi: " + e.getMessage());
            // e.printStackTrace(); // In đầy đủ stack trace nếu cần debug sâu
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi xóa file: " + relativeUrl + " - Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/sua/{id}")
    public String capNhatSanPhamChiTiet(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("spChiTiet") SPChiTiet spChiTiet, // Thêm @Valid nếu có validation
            BindingResult bindingResult, // Nhận kết quả validation
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            @RequestParam(name = "xoaAnhIds", required = false) List<Integer> xoaAnhIds,
            RedirectAttributes redirectAttributes,
            Model model) { // Thêm Model để xử lý lỗi

        // Tìm SPCT gốc trong DB
        Optional<SPChiTiet> spChiTietDBOpt = spChiTietRepo.findById(id);
        if (!spChiTietDBOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm chi tiết để cập nhật!");
            return "redirect:/san-pham-chi-tiet";
        }
        SPChiTiet spChiTietDB = spChiTietDBOpt.get();

        // Kiểm tra lỗi validation cơ bản (nếu dùng @Valid)
        if (bindingResult.hasErrors()) {
            loadDropdownData(model); // Load lại dropdowns
            // Load lại ảnh hiện có vì view cần nó
            List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhRepo.findBySpChiTiet_Id(id);
            model.addAttribute("hinhAnhList", hinhAnhList);
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập liệu!");
            // spChiTiet đã được đưa vào model bởi @ModelAttribute, giữ lại dữ liệu lỗi
            return "sp-chi-tiet/cap-nhat"; // Trả về form với lỗi
        }




        try {
            // Update fields từ form vào đối tượng DB
            // spChiTietDB.setMaSPCT(spChiTiet.getMaSPCT()); // Chỉ cập nhật nếu mã được phép sửa
            spChiTietDB.setSanPham(spChiTiet.getSanPham());
            spChiTietDB.setThuongHieu(spChiTiet.getThuongHieu()); // *** SỬA: THÊM CẬP NHẬT THƯƠNG HIỆU ***
            spChiTietDB.setMauSac(spChiTiet.getMauSac());
            spChiTietDB.setKichThuoc(spChiTiet.getKichThuoc());
            spChiTietDB.setSoLuong(spChiTiet.getSoLuong());
            spChiTietDB.setDonGia(spChiTiet.getDonGia());
            // Đảm bảo trạng thái không bị null nếu checkbox không được tick và kiểu là Boolean
            spChiTietDB.setTrangThai(spChiTiet.getTrangThai() != null && spChiTiet.getTrangThai());


            // Xóa ảnh cũ được chọn
            if (xoaAnhIds != null && !xoaAnhIds.isEmpty()) {
                List<HinhAnhSPChiTiet> anhCanXoa = hinhAnhRepo.findAllById(xoaAnhIds);
                for (HinhAnhSPChiTiet anh : anhCanXoa) {
                    // Chỉ xóa nếu ảnh đó thực sự thuộc về SPCT này (đề phòng)
                    if (anh.getSpChiTiet().getId().equals(id)) {
                        xoaFile(anh.getUrl()); // Xóa file vật lý
                        hinhAnhRepo.delete(anh); // Xóa record trong DB
                    }
                }
            }

            // Lưu ảnh mới
            if (files != null && files.length > 0 && !files[0].isEmpty()) {
                // *** CẢNH BÁO: ClassPathResource không đáng tin cậy để GHI file khi deploy JAR/WAR ***
                // Path uploadDirPath = Paths.get(uploadDir); // Dùng đường dẫn cấu hình
                Path uploadDirPath = Paths.get(new ClassPathResource("static/image").getURI()); // Tạm dùng ClasspathResource
                if (!Files.exists(uploadDirPath)) {
                    Files.createDirectories(uploadDirPath);
                }

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String originalFileName = file.getOriginalFilename();
                        String fileExtension = "";
                        if (originalFileName != null && originalFileName.contains(".")) {
                            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                        }
                        String uniqueFileName = UUID.randomUUID() + fileExtension;
                        Path filePath = uploadDirPath.resolve(uniqueFileName);

                        try (InputStream inputStream = file.getInputStream()) {
                            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                        }

                        HinhAnhSPChiTiet hinhAnh = new HinhAnhSPChiTiet();
                        hinhAnh.setSpChiTiet(spChiTietDB); // Liên kết với SPCT đang sửa
                        hinhAnh.setUrl("/image/" + uniqueFileName);
                        hinhAnh.setMoTa("Ảnh sản phẩm " + spChiTietDB.getMaSPCT());
                        hinhAnh.setTrangThai(true);
                        hinhAnhRepo.save(hinhAnh);
                    }
                }
            }

            spChiTietRepo.save(spChiTietDB); // Lưu lại SPCT đã cập nhật
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chi tiết sản phẩm thành công!");
            return "redirect:/san-pham-chi-tiet";

        } catch (Exception e) {
            e.printStackTrace(); // Quan trọng: In lỗi ra console
            loadDropdownData(model); // Load lại dropdowns
            // Load lại ảnh hiện có
            List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhRepo.findBySpChiTiet_Id(id);
            model.addAttribute("hinhAnhList", hinhAnhList);
            model.addAttribute("spChiTiet", spChiTiet); // Giữ lại dữ liệu đã nhập/sửa
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật chi tiết sản phẩm: " + e.getMessage());
            return "sp-chi-tiet/cap-nhat"; // Trả về form với lỗi
        }
    }

    @PostMapping("/xoa/{id}")
    public String xoaSPChiTiet(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<SPChiTiet> spChiTietOpt = spChiTietRepo.findById(id);
        if (spChiTietOpt.isPresent()) {
            SPChiTiet spChiTiet = spChiTietOpt.get();
            // Thay đổi trạng thái thay vì xóa cứng
            spChiTiet.setTrangThai(false);
            spChiTietRepo.save(spChiTiet);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển trạng thái chi tiết sản phẩm thành 'Không hoạt động'!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy chi tiết sản phẩm để cập nhật trạng thái!");
        }
        return "redirect:/san-pham-chi-tiet";
    }
}