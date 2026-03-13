package com.example.gestionnotes.repository;

import com.example.gestionnotes.entity.Correcteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CorrecteurRepository extends JpaRepository<Correcteur, Long> {
    Optional<Correcteur> findByPrenom(String prenom);
}