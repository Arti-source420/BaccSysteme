package com.forage.service;

import com.forage.model.Client;
import com.forage.model.Demande;
import com.forage.model.DemandeStatus;
import com.forage.model.Status;
import com.forage.repository.DemandeRepository;
import com.forage.repository.DemandeStatusRepository;
import com.forage.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeServiceImpl implements DemandeService {

    private final DemandeRepository        demandeRepository;
    private final DemandeStatusRepository  demandeStatusRepository;
    private final StatusRepository         statusRepository;

    // ─────────────────────────────────────────────────────────────
    //  LECTURE
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Demande> findAll() {
        return demandeRepository.findAllWithClient();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Demande> findById(Long id) {
        return demandeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Demande> findByClientId(Long clientId) {
        return demandeRepository.findByClientId(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Demande> search(String keyword) {
        return demandeRepository
                .findByLieuContainingIgnoreCaseOrDistrictContainingIgnoreCase(keyword, keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return demandeRepository.existsById(id);
    }

    // ─────────────────────────────────────────────────────────────
    //  CRÉATION — persiste la demande + statut initial automatique
    // ─────────────────────────────────────────────────────────────

    @Override
    public Demande creer(Demande demande) {
        // Garantir qu'on crée bien un nouvel enregistrement
        demande.setId(null);

        Demande saved = demandeRepository.save(demande);

        // Chercher le statut "En attente" ; fallback sur le 1er statut disponible
        Status statusInitial = statusRepository
                .findByLibelleIgnoreCase("cree")
                .orElseGet(() -> statusRepository.findAll()
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Aucun statut en base. Exécutez schema.sql.")));

        DemandeStatus ds = new DemandeStatus();
        ds.setDemande(saved);
        ds.setStatus(statusInitial);
        ds.setCommentaire("Demande créée");
        ds.setDate(LocalDateTime.now());
        demandeStatusRepository.save(ds);

        return saved;
    }

    // ─────────────────────────────────────────────────────────────
    //  MODIFICATION — met à jour lieu, district, date, client
    //                 sans toucher à l'historique des statuts
    // ─────────────────────────────────────────────────────────────

    @Override
    public Demande modifier(Long id, Demande source) {
        Demande existante = demandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Demande introuvable : " + id));

        existante.setLieu(source.getLieu());
        existante.setDistrict(source.getDistrict());
        existante.setDate(source.getDate());
        existante.setClient(source.getClient());

        return demandeRepository.save(existante);
    }

    // ─────────────────────────────────────────────────────────────
    //  SAVE GÉNÉRIQUE — conservé pour compatibilité (ex. tests)
    //  Comportement : création si id == null, sinon mise à jour simple
    // ─────────────────────────────────────────────────────────────

    @Override
    public Demande save(Demande demande) {
        boolean isNew = (demande.getId() == null);
        Demande saved = demandeRepository.save(demande);

        if (isNew) {
            Status statusCree = statusRepository
                    .findByLibelleIgnoreCase("cree")
                    .orElseGet(() -> statusRepository.findAll()
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                    "Aucun statut disponible en base. Veuillez exécuter le script schema.sql.")));

            DemandeStatus ds = new DemandeStatus();
            ds.setDemande(saved);
            ds.setStatus(statusCree);
            ds.setCommentaire("Demande créée");
            ds.setDate(LocalDateTime.now());
            demandeStatusRepository.save(ds);
        }

        return saved;
    }

    // ─────────────────────────────────────────────────────────────
    //  SUPPRESSION
    // ─────────────────────────────────────────────────────────────

    @Override
    public void deleteById(Long id) {
        demandeRepository.deleteById(id);
    }
}
