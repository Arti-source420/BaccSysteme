package com.forage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ForageApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForageApplication.class, args);
         System.out.println("========================================");
        System.out.println("Application de Gestion des Notes démarrée avec succès!");
        System.out.println("Accédez à l'application: http://localhost:8088");
        System.out.println("========================================");
    }
}
