package com.forage.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "devis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Devis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "montant_total", precision = 15, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_devis_id", nullable = false)
    private TypeDevis typeDevis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id")
    private Demande demande;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetailDevis> details;
}
