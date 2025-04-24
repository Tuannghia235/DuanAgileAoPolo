package org.example.registerpolo.Repository;


import org.example.registerpolo.Entity.KichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KichThuocRepo extends JpaRepository<KichThuoc, Integer> {
    @Query("SELECT COUNT(k) > 0 FROM KichThuoc k WHERE k.ma = :ma")
    boolean existsByMa(@Param("ma") String ma);
    List<KichThuoc> findByTrangThaiTrue();

}
