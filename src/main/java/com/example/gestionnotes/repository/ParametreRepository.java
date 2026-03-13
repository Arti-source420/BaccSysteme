package com.example.gestionnotes.repository;

import com.example.gestionnotes.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParametreRepository extends JpaRepository<Parametre, Long> {
    List<Parametre> findByMatiere(Matiere matiere);
    
    @Query("SELECT p FROM Parametre p WHERE p.matiere.id = :idMatiere ORDER BY p.difference")
    List<Parametre> findByMatiereIdOrderByDifference(@Param("idMatiere") Long idMatiere);
}