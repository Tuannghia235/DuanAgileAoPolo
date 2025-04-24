package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.HinhAnhSPChiTiet; // Giữ import này nếu bạn có methods liên quan đến HinhAnhSPChiTiet ở đây (dù thường là ở Repo riêng)
import org.example.registerpolo.Entity.SPChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SPChiTietRepo extends JpaRepository<SPChiTiet, Integer> {

    // Phương thức này có vẻ hơi thừa so với findAllWithFilters nếu chỉ dùng cho list view
    @Query("SELECT sp FROM SPChiTiet sp WHERE " +
            "(:keyword IS NULL OR LOWER(sp.maSPCT) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(sp.sanPham.ten) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:mau IS NULL OR sp.mauSac.id = :mau) AND " +
            "(:size IS NULL OR sp.kichThuoc.id = :size)")
    List<SPChiTiet> filterSanPham(@Param("keyword") String keyword,
                                  @Param("mau") Integer mau,
                                  @Param("size") Integer size);

    // FIX: Bổ sung điều kiện lọc theo thuongHieuId và cải thiện điều kiện tìm kiếm
    @Query("SELECT sp FROM SPChiTiet sp WHERE " +
            "(:search IS NULL OR LOWER(sp.maSPCT) LIKE LOWER(CONCAT('%', :search, '%')) " + // Tìm theo mã SPCT
            "OR LOWER(sp.sanPham.ten) LIKE LOWER(CONCAT('%', :search, '%'))) AND " + // Tìm theo tên SP (case-insensitive)
            "(:mauSacId IS NULL OR sp.mauSac.id = :mauSacId) AND " +
            "(:kichThuocId IS NULL OR sp.kichThuoc.id = :kichThuocId) AND " +
            "(:thuongHieuId IS NULL OR sp.sanPham.thuongHieu.id = :thuongHieuId)") // THÊM: Điều kiện lọc Thương hiệu
    Page<SPChiTiet> findAllWithFilters(@Param("search") String search,
                                       @Param("mauSacId") Integer mauSacId,
                                       @Param("kichThuocId") Integer kichThuocId,
                                       @Param("thuongHieuId") Integer thuongHieuId, // Tham số đã có
                                       Pageable pageable);

    // Phương thức lọc này có logic kết hợp điều kiện bằng OR (khác với AND thông thường)
    // và lọc theo tên thay vì ID cho size/color. Cần xem xét lại mục đích sử dụng
    @Query("SELECT sp FROM SPChiTiet sp " +
            "WHERE " +
            "(:thuongHieuId IS NOT NULL AND sp.sanPham.thuongHieu.id = :thuongHieuId) OR " +
            "(:gia IS NOT NULL AND " +
            "   ((:gia = '1' AND sp.donGia < 500000) OR " +
            "    (:gia = '2' AND sp.donGia BETWEEN 500000 AND 1000000) OR " +
            "    (:gia = '3' AND sp.donGia > 1000000)) ) OR " +
            "(:trangThai IS NOT NULL AND sp.trangThai = :trangThai) OR " +
            "(:size IS NOT NULL AND sp.kichThuoc.ten = :size) OR " + // Lọc theo tên size
            "(:mauSac IS NOT NULL AND sp.mauSac.ten = :mauSac)") // Lọc theo tên màu sắc
    Page<SPChiTiet> locSanPham(@Param("thuongHieuId") Integer thuongHieuId,
                               @Param("gia") String gia,
                               @Param("trangThai") Boolean trangThai,
                               @Param("size") String size,
                               @Param("mauSac") String mauSac,
                               Pageable pageable);

    // XÓA BỎ: Phương thức này đã được cung cấp bởi JpaRepository
    // Optional<SPChiTiet> findById(Integer id);

    // Các phương thức dưới đây thường nằm trong HinhAnhSPChiTietRepo
    // List<HinhAnhSPChiTiet> findBySpChiTiet(SPChiTiet spChiTiet);
    // List<HinhAnhSPChiTiet> findBySpChiTiet_Id(Integer id);
}