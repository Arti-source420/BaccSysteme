-- ============================================================
-- SYSTÈME BAC — Script SQL complet
-- ============================================================
-- Opérateurs  : id 1=<   2=>   3=<=  4=>=
-- Résolutions : id 1=plus petit  2=plus grand  3=moyenne
-- ============================================================

DROP TABLE IF EXISTS note CASCADE;
DROP TABLE IF EXISTS parametre CASCADE;
DROP TABLE IF EXISTS correcteur CASCADE;
DROP TABLE IF EXISTS candidat CASCADE;
DROP TABLE IF EXISTS matiere CASCADE;
DROP TABLE IF EXISTS operateur CASCADE;
DROP TABLE IF EXISTS resolution CASCADE;

-- ── Tables ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS candidat (
    id_candidat BIGSERIAL PRIMARY KEY,
    nom         VARCHAR(100),
    prenom      VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS matiere (
    id_matiere BIGSERIAL PRIMARY KEY,
    matiere    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS correcteur (
    id_correcteur BIGSERIAL PRIMARY KEY,
    nom           VARCHAR(100),
    prenom        VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS resolution (
    id_resolution BIGSERIAL PRIMARY KEY,
    resolution    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS operateur (
    id_operateur BIGSERIAL PRIMARY KEY,
    operateur    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS parametre (
    id_parametre  BIGSERIAL PRIMARY KEY,
    id_matiere    BIGINT           NOT NULL,
    difference    DOUBLE PRECISION NOT NULL,
    id_operateur  BIGINT           NOT NULL,
    id_resolution BIGINT           NOT NULL,
    FOREIGN KEY (id_matiere)    REFERENCES matiere(id_matiere),
    FOREIGN KEY (id_operateur)  REFERENCES operateur(id_operateur),
    FOREIGN KEY (id_resolution) REFERENCES resolution(id_resolution)
);

CREATE TABLE IF NOT EXISTS note (
    id_note       BIGSERIAL PRIMARY KEY,
    id_candidat   BIGINT           NOT NULL,
    id_matiere    BIGINT           NOT NULL,
    id_correcteur BIGINT           NOT NULL,
    note          DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (id_candidat)   REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_matiere)    REFERENCES matiere(id_matiere),
    FOREIGN KEY (id_correcteur) REFERENCES correcteur(id_correcteur)
);

-- ── Données de référence ─────────────────────────────────────

INSERT INTO operateur (operateur) VALUES
('<'), ('>'), ('<='), ('>=')
ON CONFLICT (operateur) DO NOTHING;
-- id 1 <   2 >   3 <=   4 >=

INSERT INTO resolution (resolution) VALUES
('plus petit'), ('plus grand'), ('moyenne')
ON CONFLICT (resolution) DO NOTHING;
-- id 1=plus petit   2=plus grand   3=moyenne

-- ── Matières (3 matières) ────────────────────────────────────

INSERT INTO matiere (matiere) VALUES
('JAVA'),   
('PHP'),    
('MATH')    
ON CONFLICT (matiere) DO NOTHING;

-- ── Candidats (6 candidats) ──────────────────────────────────

INSERT INTO candidat (nom, prenom) VALUES
('Rakoto',   'Jean'),       -- id=1  données existantes
('Rabe',     'Marie'),      -- id=2  données existantes
('Randria',  'Paul'),       -- id=3  test MATH conflit (diff=7)
('Rasoa',    'Julie'),      -- id=4  test MATH conflit ex-aequo (diff=6)
('Andry',    'Luc'),        -- id=5  test MATH 1 seule règle (diff=3, diff=10)
('Faniry',   'Clara')       -- id=6  test MATH 1 seule règle (diff=10)
ON CONFLICT (prenom) DO NOTHING;

-- ── Correcteurs (3 correcteurs) ──────────────────────────────

INSERT INTO correcteur (nom, prenom) VALUES
('Razafy',  'Robert'),      -- id=1
('Ranivo',  'Sophie'),      -- id=2
('Rakoton', 'Thomas')       -- id=3
ON CONFLICT (prenom) DO NOTHING;

-- ============================================================
-- PARAMÈTRES DE DÉLIBÉRATION
-- ============================================================

-- ── JAVA (id_matiere=1) — règles MUTUELLEMENT EXCLUSIVES ────
-- Comportement d'AVANT : aucun conflit possible
--   diff < 7  → plus grand  (on prend la meilleure note)
--   diff >= 7 → moyenne     (les correcteurs sont trop éloignés)

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution)
VALUES (1, 1, 7.0, 2);  -- JAVA : diff < 7  → plus grand

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution)
VALUES (1, 4, 7.0, 3);  -- JAVA : diff >= 7 → moyenne

-- ── PHP (id_matiere=2) — règles MUTUELLEMENT EXCLUSIVES ─────
-- Comportement d'AVANT : aucun conflit possible
--   diff <= 2 → plus petit  (notes proches → on est prudent)
--   diff > 2  → plus grand  (grand écart → on favorise la meilleure)

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution)
VALUES (2, 3, 2.0, 1);  -- PHP : diff <= 2 → plus petit

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution)
VALUES (2, 2, 2.0, 2);  -- PHP : diff > 2  → plus grand

-- ── MATH (id_matiere=3) — règles QUI SE CHEVAUCHENT ─────────
-- NOUVELLE FONCTIONNALITÉ : gestion des conflits
--   diff > 4  → plus grand
--   diff <= 8 → moyenne
--
--   Zone de conflit : 4 < diff <= 8
--     → les deux règles s'appliquent en même temps
--     → l'algorithme choisit par proximité au seuil
--
--   Exemple diff=7 : |7-4|=3 vs |7-8|=1 → plus proche de 8 → MOYENNE
--   Exemple diff=6 : |6-4|=2 vs |6-8|=2 → ex-aequo → seuil min=4 → PLUS GRAND

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution)
VALUES (3, 2, 4.0, 2);  -- MATH : diff > 4  → plus grand

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution)
VALUES (3, 3, 8.0, 3);  -- MATH : diff <= 8 → moyenne

-- ============================================================
-- NOTES — DONNÉES EXISTANTES (JAVA et PHP)
-- ============================================================

-- Candidat 1 (Jean) en JAVA — 3 correcteurs
-- diff = |15-10|+|15-12|+|10-12| = 5+3+2 = 10  → diff >= 7 → MOYENNE → 12.33
INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES
(1, 1, 1, 15.0),
(1, 1, 2, 10.0),
(1, 1, 3, 12.0);

-- Candidat 2 (Marie) en JAVA — 3 correcteurs
-- diff = |9-8|+|9-11|+|8-11| = 1+2+3 = 6  → diff < 7 → PLUS GRAND → 11.00
INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES
(2, 1, 1, 9.0),
(2, 1, 2, 8.0),
(2, 1, 3, 11.0);

-- Candidat 1 (Jean) en PHP — 2 correcteurs
-- diff = |10-10| = 0  → diff <= 2 → PLUS PETIT → 10.00
INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES
(1, 2, 1, 10.0),
(1, 2, 2, 10.0);

-- Candidat 2 (Marie) en PHP — 2 correcteurs
-- diff = |13-11| = 2  → diff <= 2 → PLUS PETIT → 11.00
INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES
(2, 2, 1, 13.0),
(2, 2, 2, 11.0);

-- ============================================================
-- NOTES — NOUVELLES DONNÉES (MATH) — TEST DES CONFLITS
-- ============================================================

-- ── CAS 1 : CONFLIT résolu par proximité ────────────────────
-- Candidat 3 (Paul) en MATH — 2 correcteurs
-- diff = |10-17| = 7
-- Règle  diff > 4  : 7 > 4 ✓  distance |7-4| = 3
-- Règle  diff <= 8 : 7 <= 8 ✓  distance |7-8| = 1  ← PLUS PROCHE
-- → CONFLIT → on prend diff <= 8 → MOYENNE → (10+17)/2 = 13.50
INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES
(3, 3, 1, 10.0),
(3, 3, 2, 17.0);

-- ── CAS 2 : CONFLIT ex-aequo → seuil le plus petit ──────────
-- Candidat 4 (Julie) en MATH — 2 correcteurs
-- diff = |10-16| = 6
-- Règle  diff > 4  : 6 > 4 ✓  distance |6-4| = 2
-- Règle  diff <= 8 : 6 <= 8 ✓  distance |6-8| = 2  ← ÉGALITÉ
-- → EX-AEQUO → seuil le plus petit = 4 → diff > 4 → PLUS GRAND → 16.00
INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES
(4, 3, 1, 10.0),
(4, 3, 2, 16.0);

-- ── CAS 3 : 1 seule règle (diff petite) ─────────────────────
-- Candidat 5 (Luc) en MATH — 2 correcteurs
-- diff = |8-11| = 3
-- Règle  diff > 4  : 3 > 4 ✗
-- Règle  diff <= 8 : 3 <= 8 ✓  ← SEULE RÈGLE SATISFAITE
-- → MOYENNE → (8+11)/2 = 9.50
INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES
(5, 3, 1, 8.0),
(5, 3, 2, 11.0);

-- ── CAS 4 : 1 seule règle (diff grande) ─────────────────────
-- Candidat 6 (Clara) en MATH — 2 correcteurs
-- diff = |5-15| = 10
-- Règle  diff > 4  : 10 > 4 ✓  ← SEULE RÈGLE SATISFAITE
-- Règle  diff <= 8 : 10 <= 8 ✗
-- → PLUS GRAND → 15.00
INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES
(6, 3, 1, 5.0),
(6, 3, 2, 15.0);