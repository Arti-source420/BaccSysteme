package com.forage.repository;

import com.forage.model.DetailDevis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailDevisRepository extends JpaRepository<DetailDevis, Long> {

    List<DetailDevis> findByDevisId(Long devisId);
}
