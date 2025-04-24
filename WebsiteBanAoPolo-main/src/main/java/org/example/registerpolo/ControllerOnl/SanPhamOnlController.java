package org.example.registerpolo.ControllerOnl;

import jakarta.servlet.http.HttpSession;
import org.example.registerpolo.Entity.HinhAnhSPChiTiet;
import org.example.registerpolo.Entity.SPChiTiet;
import org.example.registerpolo.Entity.SanPham;
import org.example.registerpolo.Repository.*;
import org.example.registerpolo.Service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ralph-lauren")
public class SanPhamOnlController {

    @Autowired
    private SPChiTietRepo spChiTietRepository;
    @Autowired
    private ThuongHieuRepo thuongHieuRepo;
    @Autowired
    private HinhAnhSPChiTietRepo hinhAnhSPChiTietRepository;
    @Autowired
    private MauSacRepo mauSacRepo;
    @Autowired
    private SanPhamRepo sanPhamRepo;
    @Autowired
    private HttpSession session;
    @GetMapping("/san-pham")
    public String hienThiTatCaSanPham(Model model,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(required = false) Integer thuongHieuId,
                                      @RequestParam(required = false) String gia,
                                      @RequestParam(required = false) Boolean trangThai,
                                      @RequestParam(required = false) String size,
                                      @RequestParam(required = false) String mauSac) {

        int pageSize = 6;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").descending());

        // Kiểm tra xem người dùng có chọn tiêu chí lọc nào không
        boolean isLoc = thuongHieuId != null || (gia != null && !gia.isEmpty())
                || trangThai != null || (size != null && !size.isEmpty())
                || (mauSac != null && !mauSac.isEmpty());

        Page<SPChiTiet> pageSPChiTiet;

        if (isLoc) {
            pageSPChiTiet = spChiTietRepository.locSanPham(
                    thuongHieuId, gia, trangThai, size, mauSac, pageable);
        } else {
            pageSPChiTiet = spChiTietRepository.findAll(pageable);
        }

        List<SPChiTiet> dsSPChiTiet = pageSPChiTiet.getContent();

        for (SPChiTiet spChiTiet : dsSPChiTiet) {
            List<HinhAnhSPChiTiet> hinhAnhList = hinhAnhSPChiTietRepository.findBySpChiTiet(spChiTiet);
            spChiTiet.setHinhAnhList(hinhAnhList);
        }

        // Gửi dữ liệu xuống view
        model.addAttribute("dsSPChiTiet", dsSPChiTiet);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageSPChiTiet.getTotalPages());
        model.addAttribute("dsThuongHieu", thuongHieuRepo.findAll());
        model.addAttribute("dsMauSac", mauSacRepo.findAllDistinctTenMau());

        return "BanHangOnl/SanPhamOnl";
    }
    @GetMapping("/san-pham/{id}")
    public String getProductDetail(@PathVariable("id") Integer id, Model model) {
        // Lấy chi tiết sản phẩm
        SPChiTiet spChiTiet = spChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + id));

        // Đảm bảo danh sách hình ảnh được load (tránh lazy loading khi ra view)
        if (spChiTiet.getHinhAnhList() != null) {
            spChiTiet.getHinhAnhList().size();
        }

        // Thêm chi tiết sản phẩm vào model
        model.addAttribute("spChiTiet", spChiTiet);

        // Lấy sản phẩm chính từ chi tiết
        SanPham sanPhamDangXem = spChiTiet.getSanPham();

        // Lấy danh sách sản phẩm nổi bật (khác với sản phẩm đang xem)
        List<SanPham> sanPhamNoiBat = sanPhamRepo.findTopNoiBat(PageRequest.of(0, 20)).stream()
                .filter(sp -> !sp.getId().equals(sanPhamDangXem.getId()))
                .collect(Collectors.toList());

        // Xáo trộn danh sách để lấy ngẫu nhiên
        Collections.shuffle(sanPhamNoiBat);

        // Lấy tối đa 4 sản phẩm
        List<SanPham> sanPhamNoiBatRandom = sanPhamNoiBat.stream()
                .limit(4)
                .collect(Collectors.toList());

        // Thêm vào model
        model.addAttribute("sanPhamNoiBat", sanPhamNoiBatRandom);

        return "BanHangOnl/SanPhamChiTietOnl";
    }
}
