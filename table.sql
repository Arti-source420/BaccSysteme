
DROP TABLE IF EXISTS note CASCADE;
DROP TABLE IF EXISTS parametre CASCADE;
DROP TABLE IF EXISTS correcteur CASCADE;
DROP TABLE IF EXISTS candidat CASCADE;
DROP TABLE IF EXISTS matiere CASCADE;
DROP TABLE IF EXISTS operateur CASCADE;
DROP TABLE IF EXISTS resolution CASCADE;



CREATE TABLE IF NOT EXISTS candidat (
    id_candidat BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100),
    prenom VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS matiere (
    id_matiere BIGSERIAL PRIMARY KEY,
    matiere VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS correcteur (
    id_correcteur BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100),
    prenom VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS resolution (
    id_resolution BIGSERIAL PRIMARY KEY,
    resolution VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS operateur (
    id_operateur BIGSERIAL PRIMARY KEY,
    operateur VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS parametre (
    id_parametre BIGSERIAL PRIMARY KEY,
    id_matiere BIGINT NOT NULL,
    difference DOUBLE PRECISION NOT NULL,
    id_operateur BIGINT NOT NULL,
    id_resolution BIGINT NOT NULL,
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere),
    FOREIGN KEY (id_operateur) REFERENCES operateur(id_operateur),
    FOREIGN KEY (id_resolution) REFERENCES resolution(id_resolution)
);

CREATE TABLE IF NOT EXISTS note (
    id_note BIGSERIAL PRIMARY KEY,
    id_candidat BIGINT NOT NULL,
    id_matiere BIGINT NOT NULL,
    id_correcteur BIGINT NOT NULL,
    note DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (id_candidat) REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_matiere) REFERENCES matiere(id_matiere),
    FOREIGN KEY (id_correcteur) REFERENCES correcteur(id_correcteur)
);



INSERT INTO operateur (operateur) VALUES 
('<'), ('>'), ('<='), ('>=') 
ON CONFLICT (operateur) DO NOTHING;


INSERT INTO resolution (resolution) VALUES 
('plus petit'), ('plus grand'), ('moyenne') 
ON CONFLICT (resolution) DO NOTHING;


INSERT INTO matiere (matiere) VALUES 
('JAVA'), ('PHP')
ON CONFLICT (matiere) DO NOTHING;


INSERT INTO candidat (nom, prenom) VALUES 
('Candidat1', 'Candidat1'), ('Cnadidat2', 'Candidat2')
ON CONFLICT (prenom) DO NOTHING;


INSERT INTO correcteur (nom, prenom) VALUES 
('Correcteur1', 'Robert'), ('Correcteur2', 'Sophie'), ('Correcteur3', 'Thomas')
ON CONFLICT (prenom) DO NOTHING;



INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution) 
VALUES (1, 1, 7.0, 2);  

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution) 
VALUES (1, 4, 7.0, 3);  

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution) 
VALUES (2, 3, 2.0, 1);  

INSERT INTO parametre (id_matiere, id_operateur, difference, id_resolution) 
VALUES (2, 2, 2.0, 2);  

INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES 
(1, 1, 1, 15.0),  
(1, 1, 2, 10.0),  
(1, 1, 3, 12.0), 
(2, 1, 1, 9.0),
(2, 1, 2, 8.0),
(2, 1, 3, 11.0);

INSERT INTO note (id_candidat, id_matiere, id_correcteur, note) VALUES 
(1, 2, 1, 10.0),  
(1, 2, 2, 10.0),  

(2, 2, 1, 13.0),
(2, 2, 2, 11.0);
















