package com.forage.service;

import com.forage.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientService {

    List<Client> findAll();

    Optional<Client> findById(Long id);

    Client save(Client client);

    void deleteById(Long id);

    List<Client> search(String nom);

    boolean existsById(Long id);
}
