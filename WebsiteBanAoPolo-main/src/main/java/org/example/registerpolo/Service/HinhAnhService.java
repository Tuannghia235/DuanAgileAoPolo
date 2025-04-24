package org.example.registerpolo.Service;

import org.example.registerpolo.Entity.HinhAnhSPChiTiet;
import org.example.registerpolo.Repository.HinhAnhSPChiTietRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HinhAnhService {

    @Autowired
    private HinhAnhSPChiTietRepo hinhAnhRepository;

    // Tìm danh sách ảnh theo ID SPChiTiet
    public List<HinhAnhSPChiTiet> findBySpChiTietId(Integer idSPChiTiet) {
        return hinhAnhRepository.findBySpChiTiet_Id(idSPChiTiet);
    }

    // Tìm ảnh theo ID ảnh
    public Optional<HinhAnhSPChiTiet> findById(Integer id) {
        return hinhAnhRepository.findById(id);
    }

    // Lưu ảnh mới hoặc cập nhật
    public HinhAnhSPChiTiet save(HinhAnhSPChiTiet entity) {
        return hinhAnhRepository.save(entity);
    }

    // Xóa ảnh theo ID
    public void xoaAnh(Integer id) {
        Optional<HinhAnhSPChiTiet> optional = hinhAnhRepository.findById(id);
        if (optional.isPresent()) {
            HinhAnhSPChiTiet img = optional.get();

            // Nếu bạn lưu ảnh ở thư mục vật lý => thêm code xóa file ở đây
            // Ví dụ: fileStorageService.delete(img.getUrl());

            hinhAnhRepository.deleteById(id);
        }
    }
}
