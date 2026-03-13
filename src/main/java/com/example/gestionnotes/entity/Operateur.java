package com.example.gestionnotes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "operateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_operateur")
    private Long id;
    
    @Column(name = "operateur", nullable = false, unique = true)
    private String symbole; // "<", ">", ">=", "<="
}