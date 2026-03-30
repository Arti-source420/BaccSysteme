package com.forage.service;

import com.forage.dto.DevisFormDto;
import com.forage.model.Devis;

import java.util.List;
import java.util.Optional;

public interface DevisService {

    List<Devis> findAll();

    Optional<Devis> findById(Long id);

    /** Crée ou met à jour un devis depuis le DTO formulaire. */
    Devis save(DevisFormDto dto);

    void deleteById(Long id);

    boolean existsById(Long id);
}
