package com.example.gestionnotes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "correcteur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Correcteur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_correcteur")
    private Long id;
    
    @Column(name = "nom")
    private String nom;
    
    @Column(name = "prenom", nullable = false, unique = true)
    private String prenom;
}