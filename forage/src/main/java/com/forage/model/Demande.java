package com.forage.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "demandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"client", "demandeStatuses", "devis"})
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date est obligatoire")
    @Column(nullable = false)
    private LocalDate date;

    @NotBlank(message = "Le lieu est obligatoire")
    @Column(nullable = false, length = 255)
    private String lieu;

    @NotBlank(message = "Le district est obligatoire")
    @Column(nullable = false, length = 255)
    private String district;

    // Pas de @NotNull ici : le client est reçu via @RequestParam clientId
    // et injecté manuellement dans le controller APRÈS la validation Bean
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DemandeStatus> demandeStatuses;

    @OneToMany(mappedBy = "demande", fetch = FetchType.LAZY)
    private List<Devis> devis;
}
