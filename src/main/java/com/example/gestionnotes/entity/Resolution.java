package com.example.gestionnotes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "resolution")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resolution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resolution")
    private Long id;
    
    @Column(name = "resolution", nullable = false, unique = true)
    private String nom; // "plus petit", "plus grand", "moyenne"
}