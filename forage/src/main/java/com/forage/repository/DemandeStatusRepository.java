package com.forage.repository;

import com.forage.model.DemandeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeStatusRepository extends JpaRepository<DemandeStatus, Long> {

    @Query("SELECT ds FROM DemandeStatus ds JOIN FETCH ds.status WHERE ds.demande.id = :demandeId ORDER BY ds.date DESC")
    List<DemandeStatus> findByDemandeIdOrderByDateDesc(@Param("demandeId") Long demandeId);
}
