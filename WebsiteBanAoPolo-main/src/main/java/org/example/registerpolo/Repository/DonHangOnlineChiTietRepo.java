package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.DonHangOnlineChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonHangOnlineChiTietRepo extends JpaRepository<DonHangOnlineChiTiet, Integer> {
    List<DonHangOnlineChiTiet> findByDonHangOnlineId(Integer donHangOnlineId);
}
