package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.HinhAnhSPChiTiet;
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

    @Query("SELECT sp FROM SPChiTiet sp WHERE " +
            "(:keyword IS NULL OR LOWER(sp.maSPCT) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(sp.sanPham.ten) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:mau IS NULL OR sp.mauSac.id = :mau) AND " +
            "(:size IS NULL OR sp.kichThuoc.id = :size)")
    List<SPChiTiet> filterSanPham(@Param("keyword") String keyword,
                                  @Param("mau") Integer mau,
                                  @Param("size") Integer size);

    @Query("SELECT sp FROM SPChiTiet sp WHERE " +
            "(:search IS NULL OR sp.sanPham.ten LIKE %:search%) AND " +
            "(:mauSacId IS NULL OR sp.mauSac.id = :mauSacId) AND " +
            "(:kichThuocId IS NULL OR sp.kichThuoc.id = :kichThuocId)")
    Page<SPChiTiet> findAllWithFilters(@Param("search") String search,
                                       @Param("mauSacId") Integer mauSacId,
                                       @Param("kichThuocId") Integer kichThuocId,
                                       Pageable pageable);

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


    Optional<SPChiTiet> findById(Integer id);
}


