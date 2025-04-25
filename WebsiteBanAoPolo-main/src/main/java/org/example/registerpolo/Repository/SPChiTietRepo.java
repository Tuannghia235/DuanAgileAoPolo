package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.SPChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SPChiTietRepo extends JpaRepository<SPChiTiet, Integer> {

    boolean existsByMaSPCT(String maSPCT); // Thêm hàm kiểm tra tồn tại mã SPCT
    boolean existsByMaSPCTAndIdNot(String maSPCT, Integer id); // Thêm hàm kiểm tra tồn tại mã SPCT khác ID hiện tại


    @Query("SELECT sp FROM SPChiTiet sp WHERE " +
            "(:keyword IS NULL OR LOWER(sp.maSPCT) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(sp.sanPham.ten) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:mau IS NULL OR sp.mauSac.id = :mau) AND " +
            "(:size IS NULL OR sp.kichThuoc.id = :size)")
    List<SPChiTiet> filterSanPham(@Param("keyword") String keyword,
                                  @Param("mau") Integer mau,
                                  @Param("size") Integer size);

    // Query tìm kiếm và lọc chính cho trang danh sách (đã bao gồm thuongHieuId và lọc theo sanPham.trangThai)
    @Query("SELECT sp FROM SPChiTiet sp JOIN FETCH sp.sanPham p JOIN FETCH p.thuongHieu th JOIN FETCH sp.mauSac ms JOIN FETCH sp.kichThuoc kt WHERE " +
            "p.trangThai = true AND " + // <-- THÊM ĐIỀU KIỆN LỌC Ở ĐÂY
            "(:search IS NULL OR LOWER(sp.maSPCT) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.ten) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:mauSacId IS NULL OR ms.id = :mauSacId) AND " +
            "(:kichThuocId IS NULL OR kt.id = :kichThuocId) AND " +
            "(:thuongHieuId IS NULL OR th.id = :thuongHieuId)")

    Page<SPChiTiet> findAllWithFilters(@Param("search") String search,
                                       @Param("mauSacId") Integer mauSacId,
                                       @Param("kichThuocId") Integer kichThuocId,
                                       @Param("thuongHieuId") Integer thuongHieuId,
                                       Pageable pageable);

    // Query lọc với logic OR và lọc theo tên (giữ nguyên theo yêu cầu)
    // Lưu ý: Query này KHÔNG tự động lọc theo p.trangThai = true trừ khi bạn thêm nó vào đây nếu cần
    @Query("SELECT sp FROM SPChiTiet sp JOIN sp.sanPham p WHERE " + // Thêm JOIN tới SanPham để có thể lọc nếu cần
            "(" + // Bọc các điều kiện OR trong ngoặc đơn
            "(:thuongHieuId IS NOT NULL AND p.thuongHieu.id = :thuongHieuId) OR " +
            "(:gia IS NOT NULL AND " +
            "   ((:gia = '1' AND sp.donGia < 500000) OR " +
            "    (:gia = '2' AND sp.donGia BETWEEN 500000 AND 1000000) OR " +
            "    (:gia = '3' AND sp.donGia > 1000000)) ) OR " +
            "(:trangThaiSPCT IS NOT NULL AND sp.trangThai = :trangThaiSPCT) OR " + // Đổi tên param tránh trùng với thuộc tính
            "(:size IS NOT NULL AND sp.kichThuoc.ten = :size) OR " +
            "(:mauSac IS NOT NULL AND sp.mauSac.ten = :mauSac)" +
            ")"
            // Nếu muốn query `locSanPham` cũng chỉ lấy sản phẩm hoạt động, thêm dòng dưới:
            // + " AND p.trangThai = true"
    )
    Page<SPChiTiet> locSanPham(@Param("thuongHieuId") Integer thuongHieuId,
                               @Param("gia") String gia,
                               @Param("trangThaiSPCT") Boolean trangThaiSPCT, // Đổi tên param
                               @Param("size") String size,
                               @Param("mauSac") String mauSac,
                               Pageable pageable);

    List<SPChiTiet> findByTrangThaiTrue();
    // Các phương thức liên quan đến HinhAnhSPChiTiet nên nằm trong HinhAnhSPChiTietRepo
    // Ví dụ: List<HinhAnhSPChiTiet> findBySpChiTiet_Id(Integer spctId); (Trong HinhAnhSPChiTietRepo)
}