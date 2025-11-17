CREATE TABLE IF NOT EXISTS employes (
    id_employe SERIAL PRIMARY KEY,
    nom VARCHAR(50),
    prenom VARCHAR(50),
    age SMALLINT,   
    mail VARCHAR(100) UNIQUE,
    valide BOOLEAN
);

INSERT INTO employes (nom, prenom,age,mail, valide) VALUES
('Dupont', 'Jean',35, 'jean.dupont@example.com', TRUE),
('Martin', 'Claire',25, 'claire.martin@example.com', FALSE);
