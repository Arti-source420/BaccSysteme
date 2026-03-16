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
('Python')    
ON CONFLICT (matiere) DO NOTHING;

-- ── Candidats (6 candidats) ──────────────────────────────────

INSERT INTO candidat (nom, prenom) VALUES
-- ('Rakoto',   'Jean'),       -- id=1  données existantes
-- ('Rabe',     'Marie'),      -- id=2  données existantes
-- ('Randria',  'Paul'),       -- id=3  test MATH conflit (diff=7)
-- ('Rasoa',    'Julie'),      -- id=4  test MATH conflit ex-aequo (diff=6)
-- ('Andry',    'Luc'),        -- id=5  test MATH 1 seule règle (diff=3, diff=10)
('Faniry',   'Clara')       -- id=6  test MATH 1 seule règle (diff=10)
ON CONFLICT (prenom) DO NOTHING;

-- ── Correcteurs (3 correcteurs) ──────────────────────────────

INSERT INTO correcteur (nom, prenom) VALUES
('correcteur1',  'Robert'),      -- id=1      
('correcteur2', 'Thomas')       -- id=2
ON CONFLICT (prenom) DO NOTHING;


INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution)
VALUES (3, 2, 4.0, 1);  

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution)
VALUES (3, 2, 1.0, 3);  


--

INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES
(1, 3, 1, 14.5),
(1, 3, 2, 13.0);

