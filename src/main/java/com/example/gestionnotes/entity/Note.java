package com.example.gestionnotes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "note")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_note")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;
    
    @ManyToOne
    @JoinColumn(name = "id_matiere", nullable = false)
    private Matiere matiere;
    
    @ManyToOne
    @JoinColumn(name = "id_correcteur", nullable = false)
    private Correcteur correcteur;
    
    @Column(name = "note", nullable = false)
    private Double valeur;
}