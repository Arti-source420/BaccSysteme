package com.forage.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "details_devis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetailDevis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String libelle;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;
}
