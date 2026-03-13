package com.example.gestionnotes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "parametre")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parametre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametre")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_matiere", nullable = false)
    private Matiere matiere;
    
    @ManyToOne
    @JoinColumn(name = "id_operateur", nullable = false)
    private Operateur operateur;
    
    @Column(name = "difference", nullable = false)
    private Double difference;
    
    @ManyToOne
    @JoinColumn(name = "id_resolution", nullable = false)
    private Resolution resolution;
}