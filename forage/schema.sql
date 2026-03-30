
CREATE DATABASE forage;

CREATE TABLE IF NOT EXISTS clients (
    id      BIGSERIAL PRIMARY KEY,
    nom     VARCHAR(255) NOT NULL,
    contact VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS status (
    id      BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS types_devis (
    id      BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS demandes (
    id       BIGSERIAL PRIMARY KEY,
    date     DATE         NOT NULL,
    lieu     VARCHAR(255) NOT NULL,
    district VARCHAR(255) NOT NULL,
    client_id BIGINT      NOT NULL,
    CONSTRAINT fk_demande_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS devis (
    id            BIGSERIAL PRIMARY KEY,
    date          DATE   NOT NULL,
    type_devis_id BIGINT NOT NULL,
    demande_id    BIGINT,
    CONSTRAINT fk_devis_type    FOREIGN KEY (type_devis_id) REFERENCES types_devis(id),
    CONSTRAINT fk_devis_demande FOREIGN KEY (demande_id)    REFERENCES demandes(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS details_devis (
    id            BIGSERIAL      PRIMARY KEY,
    libelle       VARCHAR(255)   NOT NULL,
    prix_unitaire DECIMAL(15, 2) NOT NULL,
    quantite      INTEGER        NOT NULL CHECK (quantite > 0),
    date          DATE           NOT NULL,
    devis_id      BIGINT         NOT NULL,
    CONSTRAINT fk_details_devis FOREIGN KEY (devis_id) REFERENCES devis(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demande_status (
    id          BIGSERIAL  PRIMARY KEY,
    date        TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    demande_id  BIGINT     NOT NULL,
    status_id   BIGINT     NOT NULL,
    CONSTRAINT fk_demande_status_demande FOREIGN KEY (demande_id) REFERENCES demandes(id) ON DELETE CASCADE,
    CONSTRAINT fk_demande_status_status  FOREIGN KEY (status_id)  REFERENCES status(id)
);

-- ══════════════════════════════════════════
-- DONNÉES DE RÉFÉRENCE
-- ══════════════════════════════════════════
INSERT INTO status (libelle) VALUES ('cree'),('En cours'),('Terminée'),('Annulée');
INSERT INTO types_devis (libelle) VALUES ('Forage manuel'),('Forage motorisé'),('Forage profond');

-- ══════════════════════════════════════════
-- CLIENTS
-- ══════════════════════════════════════════
INSERT INTO clients (nom, contact) VALUES
  ('Rakoto Jean',       '034 12 345 67'),
  ('Rabe Marie',        '033 98 765 43'),
  ('Andrianaivo Paul',  'andrianaivo.paul@gmail.com'),
  ('Rasoa Hanta',       '032 55 111 22');

-- ══════════════════════════════════════════
-- DEMANDES
-- ══════════════════════════════════════════
INSERT INTO demandes (date, lieu, district, client_id) VALUES
  ('2024-01-15','Ambohimanga','Manjakandriana',             1),
  ('2024-02-03','Anosibe',    'Antananarivo Atsimondrano',  2),
  ('2024-03-20','Mahitsy',    'Ambohidratrimo',             3),
  ('2024-04-10','Tanjombato', 'Antananarivo Atsimondrano',  4);

-- ══════════════════════════════════════════
-- DEVIS (sans montant_total)
-- ══════════════════════════════════════════
INSERT INTO devis (date, type_devis_id, demande_id) VALUES
  ('2024-01-20', 1, 1),
  ('2024-02-08', 2, 2),
  ('2024-03-25', 3, 3),
  ('2024-04-15', 2, 4);

-- ══════════════════════════════════════════
-- DETAILS_DEVIS (prix_unitaire + quantite + date)
-- ══════════════════════════════════════════
INSERT INTO details_devis (libelle, prix_unitaire, quantite, date, devis_id) VALUES
  ('Main d''œuvre forage manuel',    2000000.00, 1, '2024-01-20', 1),
  ('Fourniture tuyaux PVC',           750000.00, 2, '2024-01-20', 1),
  ('Transport et déplacement',        500000.00, 2, '2024-01-21', 1),

  ('Location foreuse motorisée',     4000000.00, 1, '2024-02-08', 2),
  ('Carburant et lubrifiants',         250000.00, 5, '2024-02-08', 2),
  ('Main d''œuvre spécialisée',      3500000.00, 1, '2024-02-09', 2),

  ('Forage profond 80m',             9000000.00, 1, '2024-03-25', 3),
  ('Pompe immergée + installation',  2000000.00, 2, '2024-03-25', 3),
  ('Génie civil (dalle, margelle)',  2000000.00, 1, '2024-03-26', 3),

  ('Location foreuse semi-lourde',   3200000.00, 1, '2024-04-15', 4),
  ('Main d''œuvre et encadrement',   1000000.00, 2, '2024-04-15', 4),
  ('Fournitures et consommables',     500000.00, 2, '2024-04-16', 4);

-- ══════════════════════════════════════════
-- DEMANDE_STATUS
-- ══════════════════════════════════════════
INSERT INTO demande_status (date, commentaire, demande_id, status_id) VALUES
  ('2024-01-15 08:00:00','Demande reçue, en attente d''étude',         1, 1),
  ('2024-02-03 09:30:00','Devis validé, travaux en cours',             2, 2),
  ('2024-03-20 10:00:00','Forage terminé, réception effectuée',        3, 3),
  ('2024-04-10 14:00:00','Client injoignable, dossier mis en attente', 4, 1);
