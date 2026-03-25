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
public class DemandeStatusServiceImpl implements DemandeStatusService {

    private final DemandeStatusRepository demandeStatusRepository;
    private final DemandeRepository demandeRepository;
    private final StatusRepository statusRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DemandeStatus> findByDemandeId(Long demandeId) {
        return demandeStatusRepository.findByDemandeIdOrderByDateDesc(demandeId);
    }

    @Override
    public DemandeStatus ajouterStatus(Long demandeId, Long statusId, String commentaire) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Status introuvable: " + statusId));

        DemandeStatus ds = new DemandeStatus();
        ds.setDemande(demande);
        ds.setStatus(status);
        ds.setCommentaire(commentaire);
        ds.setDate(LocalDateTime.now());
        return demandeStatusRepository.save(ds);
    }

    @Override
    public void deleteById(Long id) {
        demandeStatusRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandeStatus> findById(Long id) {
        return demandeStatusRepository.findById(id);
    }
}
