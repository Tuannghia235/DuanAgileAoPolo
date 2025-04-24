package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.DonHangOnline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonHangOnlineRepo extends JpaRepository<DonHangOnline, Integer> {
    DonHangOnline findTopByKhachHangIdOrderByNgayDatHangDesc(Integer khachHangId);
}
