package com.example.gestionnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.example.gestionnotes.entity")
@EnableJpaRepositories(basePackages = "com.example.gestionnotes.repository")
public class GestionNotesApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GestionNotesApplication.class, args);
        
        // Message de bienvenue dans la console
        System.out.println("========================================");
        System.out.println("Application de Gestion des Notes démarrée avec succès!");
        System.out.println("Accédez à l'application: http://localhost:8088");
        System.out.println("========================================");
    }
}