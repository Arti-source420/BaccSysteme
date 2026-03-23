package com.forage.service;

import com.forage.model.Demande;
import com.forage.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeServiceImpl implements DemandeService {

    private final DemandeRepository demandeRepository;

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
        return demandeRepository.save(demande);
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
