package com.example.gestionnotes.repository;

import com.example.gestionnotes.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MatiereRepository extends JpaRepository<Matiere, Long> {
    Optional<Matiere> findByNom(String nom);
}