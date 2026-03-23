package com.forage.service;

import com.forage.model.Client;
import com.forage.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    @Override
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public void deleteById(Long id) {
        clientRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> search(String nom) {
        return clientRepository.findByNomContainingIgnoreCase(nom);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return clientRepository.existsById(id);
    }
}
