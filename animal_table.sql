-- SQL Script to create the animal table

CREATE TABLE IF NOT EXISTS `animal` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `espece` varchar(100) NOT NULL,
  `race` varchar(100) DEFAULT NULL,
  `poids` float DEFAULT NULL,
  `etatSante` varchar(50) DEFAULT 'Sain',
  `userId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_animal_user` (`userId`),
  CONSTRAINT `fk_animal_user` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
