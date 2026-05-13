-- ============================================================
--  Agrimans – Création de la table `demande`
--  À exécuter dans la base de données `agrimans`
-- ============================================================

USE agrimans;

CREATE TABLE IF NOT EXISTS `demande` (
    `id`              INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `agriculteur_id`  INT          NOT NULL,
    `equipement_id`   INT          NULL,
    `nom_equipement`  VARCHAR(255) NULL,
    `type_demande`    VARCHAR(50)  NULL  DEFAULT 'EQUIPEMENT_EXISTANT',
    `description`     TEXT         NULL,
    `commentaire`     TEXT         NULL,
    `quantite`        INT          NULL  DEFAULT 1,
    `statut`          VARCHAR(30)  NOT NULL DEFAULT 'EN_ATTENTE',
    `reponse_chef`    TEXT         NULL,
    `date_demande`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `date_traitement` DATETIME     NULL,

    CONSTRAINT `fk_demande_user`
        FOREIGN KEY (`agriculteur_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,

    CONSTRAINT `fk_demande_equipement`
        FOREIGN KEY (`equipement_id`) REFERENCES `equipement` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Exemple de données de test (optionnel)
-- INSERT INTO demande (agriculteur_id, description, statut) VALUES (2, 'Test demande', 'EN_ATTENTE');
