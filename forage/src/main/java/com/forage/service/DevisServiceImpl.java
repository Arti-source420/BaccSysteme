package com.forage.service;

import com.forage.dto.DevisFormDto;
import com.forage.model.Demande;
import com.forage.model.DetailDevis;
import com.forage.model.Devis;
import com.forage.model.TypeDevis;
import com.forage.repository.DemandeRepository;
import com.forage.repository.DevisRepository;
import com.forage.repository.TypeDevisRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DevisServiceImpl implements DevisService {

    private final DevisRepository      devisRepository;
    private final DemandeRepository    demandeRepository;
    private final TypeDevisRepository  typeDevisRepository;

    // ──────────────────────────────────────────────────
    // LECTURE
    // ──────────────────────────────────────────────────

    @Override
    public List<Devis> findAll() {
        return devisRepository.findAllWithRelations();
    }

    @Override
    public Optional<Devis> findById(Long id) {
        return devisRepository.findByIdWithDetails(id);
    }

    @Override
    public boolean existsById(Long id) {
        return devisRepository.existsById(id);
    }

    // ──────────────────────────────────────────────────
    // ÉCRITURE
    // ──────────────────────────────────────────────────

    @Override
    @Transactional
    public Devis save(DevisFormDto dto) {

        // 1. Résoudre les associations obligatoires
        TypeDevis typeDevis = typeDevisRepository.findById(dto.getTypeDevisId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Type de devis introuvable : id=" + dto.getTypeDevisId()));

        Demande demande = demandeRepository.findById(dto.getDemandeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Demande introuvable : id=" + dto.getDemandeId()));

        // 2. Charger l'entité existante (édition) ou en créer une nouvelle
        Devis devis;
        if (dto.getId() != null) {
            devis = devisRepository.findByIdWithDetails(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Devis introuvable : id=" + dto.getId()));
            // Vider les anciennes lignes : orphanRemoval les supprime en base
            devis.getDetails().clear();
        } else {
            devis = new Devis();
        }

        // 3. Champs scalaires
        devis.setDate(dto.getDate());
        devis.setTypeDevis(typeDevis);
        devis.setDemande(demande);

        // 4. Reconstruire les lignes depuis le DTO
        List<DetailDevis> nouvellesLignes = new ArrayList<>();
        for (DevisFormDto.LigneDto ligneDto : dto.getLignes()) {

            if (estLigneVide(ligneDto)) continue; // tolérance UI

            DetailDevis detail = new DetailDevis();
            detail.setLibelle(ligneDto.getLibelle().trim());
            detail.setPrixUnitaire(ligneDto.getPrixUnitaire());
            detail.setQuantite(ligneDto.getQuantite());
            detail.setDate(ligneDto.getDate());
            detail.setDevis(devis);          // FK obligatoire
            nouvellesLignes.add(detail);
        }

        if (nouvellesLignes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Un devis doit comporter au moins une ligne de détail.");
        }

        devis.getDetails().addAll(nouvellesLignes);

        // 5. Persistance (cascade ALL + orphanRemoval gèrent les détails)
        return devisRepository.save(devis);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!devisRepository.existsById(id)) {
            throw new EntityNotFoundException("Devis introuvable : id=" + id);
        }
        devisRepository.deleteById(id);
    }

    // ──────────────────────────────────────────────────
    // UTILITAIRE PRIVÉ
    // ──────────────────────────────────────────────────

    private boolean estLigneVide(DevisFormDto.LigneDto l) {
        return (l.getLibelle() == null || l.getLibelle().isBlank())
                && l.getPrixUnitaire() == null
                && l.getQuantite() == null;
    }
}
