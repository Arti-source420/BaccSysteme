package com.forage.service;

import com.forage.model.Demande;

import java.util.List;
import java.util.Optional;

public interface DemandeService {

    List<Demande> findAll();

    Optional<Demande> findById(Long id);

    /** Création : persiste la demande ET ajoute le statut initial "En attente" */
    Demande creer(Demande demande);

    /** Modification : met à jour uniquement les champs de la demande, sans toucher aux statuts */
    Demande modifier(Long id, Demande demande);

    /** Garde la compatibilité avec l'existant (utilisé en interne) */
    Demande save(Demande demande);

    void deleteById(Long id);

    List<Demande> findByClientId(Long clientId);

    List<Demande> search(String keyword);

    boolean existsById(Long id);
}
