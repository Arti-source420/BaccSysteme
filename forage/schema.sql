-- Forage Database Schema (PostgreSQL)
-- Run this script to initialize the database

CREATE DATABASE forage;
\c forage;

CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    contact VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS status (
    id BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS types_devis (
    id BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS demandes (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    lieu VARCHAR(255) NOT NULL,
    district VARCHAR(255) NOT NULL,
    client_id BIGINT NOT NULL,
    CONSTRAINT fk_demande_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS devis (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    montant_total DECIMAL(15, 2) DEFAULT 0.00,
    type_devis_id BIGINT NOT NULL,
    demande_id BIGINT,
    CONSTRAINT fk_devis_type FOREIGN KEY (type_devis_id) REFERENCES types_devis(id),
    CONSTRAINT fk_devis_demande FOREIGN KEY (demande_id) REFERENCES demandes(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS details_devis (
    id BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    montant DECIMAL(15, 2) NOT NULL,
    devis_id BIGINT NOT NULL,
    CONSTRAINT fk_details_devis FOREIGN KEY (devis_id) REFERENCES devis(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demande_status (
    id BIGSERIAL PRIMARY KEY,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    demande_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    CONSTRAINT fk_demande_status_demande FOREIGN KEY (demande_id) REFERENCES demandes(id) ON DELETE CASCADE,
    CONSTRAINT fk_demande_status_status FOREIGN KEY (status_id) REFERENCES status(id)
);

-- Sample data
INSERT INTO status (libelle) VALUES ('En attente'), ('En cours'), ('Terminée'), ('Annulée');
INSERT INTO types_devis (libelle) VALUES ('Forage manuel'), ('Forage motorisé'), ('Forage profond');
