package com.forage.service;

import com.forage.model.DemandeStatus;

import java.util.List;
import java.util.Optional;

public interface DemandeStatusService {

    /** Récupère tous les statuts d'une demande, triés par date DESC */
    List<DemandeStatus> findByDemandeId(Long demandeId);

    /** Ajoute un nouveau statut à une demande */
    DemandeStatus ajouterStatus(Long demandeId, Long statusId, String commentaire);

    /** Supprime une entrée de suivi */
    void deleteById(Long id);

    Optional<DemandeStatus> findById(Long id);
}
