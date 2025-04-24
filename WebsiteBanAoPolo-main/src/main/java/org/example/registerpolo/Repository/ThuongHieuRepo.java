package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThuongHieuRepo extends JpaRepository<ThuongHieu, Integer> {
}
