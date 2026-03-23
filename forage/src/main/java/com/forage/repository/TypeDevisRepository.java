package com.forage.repository;

import com.forage.model.TypeDevis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDevisRepository extends JpaRepository<TypeDevis, Long> {
}
