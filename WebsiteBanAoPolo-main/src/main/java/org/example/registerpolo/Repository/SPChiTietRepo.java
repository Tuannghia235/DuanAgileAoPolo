package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.SPChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
// import java.util.Optional; // Không cần import Optional vì findById đã có sẵn

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

    // Query tìm kiếm và lọc chính cho trang danh sách (đã bao gồm thuongHieuId)
    @Query("SELECT sp FROM SPChiTiet sp JOIN FETCH sp.sanPham p JOIN FETCH p.thuongHieu th JOIN FETCH sp.mauSac ms JOIN FETCH sp.kichThuoc kt WHERE " +
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
    @Query("SELECT sp FROM SPChiTiet sp " +
            "WHERE " +
            "(:thuongHieuId IS NOT NULL AND sp.sanPham.thuongHieu.id = :thuongHieuId) OR " +
            "(:gia IS NOT NULL AND " +
            "   ((:gia = '1' AND sp.donGia < 500000) OR " +
            "    (:gia = '2' AND sp.donGia BETWEEN 500000 AND 1000000) OR " +
            "    (:gia = '3' AND sp.donGia > 1000000)) ) OR " +
            "(:trangThai IS NOT NULL AND sp.trangThai = :trangThai) OR " +
            "(:size IS NOT NULL AND sp.kichThuoc.ten = :size) OR " +
            "(:mauSac IS NOT NULL AND sp.mauSac.ten = :mauSac)")
    Page<SPChiTiet> locSanPham(@Param("thuongHieuId") Integer thuongHieuId,
                               @Param("gia") String gia,
                               @Param("trangThai") Boolean trangThai,
                               @Param("size") String size,
                               @Param("mauSac") String mauSac,
                               Pageable pageable);

    // Các phương thức liên quan đến HinhAnhSPChiTiet nên nằm trong HinhAnhSPChiTietRepo
    // Ví dụ: List<HinhAnhSPChiTiet> findBySpChiTiet_Id(Integer spctId); (Trong HinhAnhSPChiTietRepo)
}