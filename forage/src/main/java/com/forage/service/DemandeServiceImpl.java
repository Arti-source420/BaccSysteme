package com.forage.service;

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

    private final DemandeRepository demandeRepository;
    private final DemandeStatusRepository demandeStatusRepository;
    private final StatusRepository statusRepository;

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
    public Demande save(Demande demande) {
        boolean isNew = (demande.getId() == null);
        Demande saved = demandeRepository.save(demande);

        // À la création uniquement : ajouter le statut "Créé" automatiquement
        if (isNew) {
            Status statusCree = statusRepository.findByLibelleIgnoreCase("En attente")
                    .orElseGet(() -> {
                        // Fallback : prendre le premier statut disponible
                        return statusRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                        "Aucun statut disponible en base. Veuillez exécuter le script schema.sql."));
                    });

            DemandeStatus ds = new DemandeStatus();
            ds.setDemande(saved);
            ds.setStatus(statusCree);
            ds.setCommentaire("Demande créée");
            ds.setDate(LocalDateTime.now());
            demandeStatusRepository.save(ds);
        }

        return saved;
    }

    @Override
    public void deleteById(Long id) {
        demandeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Demande> findByClientId(Long clientId) {
        return demandeRepository.findByClientId(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Demande> search(String keyword) {
        return demandeRepository.findByLieuContainingIgnoreCaseOrDistrictContainingIgnoreCase(keyword, keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return demandeRepository.existsById(id);
    }
}
