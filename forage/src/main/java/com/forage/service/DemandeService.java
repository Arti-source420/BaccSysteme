package com.forage.service;

import com.forage.model.Demande;

import java.util.List;
import java.util.Optional;

public interface DemandeService {

    List<Demande> findAll();

    Optional<Demande> findById(Long id);

    Demande save(Demande demande);

    void deleteById(Long id);

    List<Demande> findByClientId(Long clientId);

    List<Demande> search(String keyword);

    boolean existsById(Long id);
}
