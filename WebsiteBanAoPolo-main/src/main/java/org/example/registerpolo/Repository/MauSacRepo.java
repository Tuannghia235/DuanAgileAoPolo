package org.example.registerpolo.Repository;

import org.example.registerpolo.Entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MauSacRepo extends JpaRepository<MauSac, Integer> {
    @Query("SELECT DISTINCT m.ten FROM MauSac m")
    List<String> findAllDistinctTenMau();
}
