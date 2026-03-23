package com.forage.repository;

import com.forage.model.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {

    List<Demande> findByClientId(Long clientId);

    @Query("SELECT d FROM Demande d JOIN FETCH d.client ORDER BY d.date DESC")
    List<Demande> findAllWithClient();

    List<Demande> findByLieuContainingIgnoreCaseOrDistrictContainingIgnoreCase(String lieu, String district);
}
