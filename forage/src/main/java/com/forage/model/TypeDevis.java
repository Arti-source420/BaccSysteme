package com.forage.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "types_devis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TypeDevis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String libelle;
}
