package com.forage.repository;

import com.forage.model.Devis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {

    /**
     * Tous les devis avec typeDevis et demande+client chargés en une requête
     * (évite le problème N+1 dans la vue liste).
     */
    @Query("""
           SELECT d FROM Devis d
           JOIN  FETCH d.typeDevis
           LEFT  JOIN FETCH d.demande dem
           LEFT  JOIN FETCH dem.client
           ORDER BY d.date DESC
           """)
    List<Devis> findAllWithRelations();

    /**
     * Un devis complet avec ses lignes de détail — utilisé dans le formulaire
     * d'édition et la page de détail.
     */
    @Query("""
           SELECT d FROM Devis d
           JOIN  FETCH d.typeDevis
           LEFT  JOIN FETCH d.demande dem
           LEFT  JOIN FETCH dem.client
           LEFT  JOIN FETCH d.details
           WHERE d.id = :id
           """)
    Optional<Devis> findByIdWithDetails(@Param("id") Long id);

    /** Devis liés à une demande donnée (pour la page détail demande). */
    @Query("""
           SELECT d FROM Devis d
           JOIN  FETCH d.typeDevis
           WHERE d.demande.id = :demandeId
           ORDER BY d.date DESC
           """)
    List<Devis> findByDemandeId(@Param("demandeId") Long demandeId);
}
