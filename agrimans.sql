-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : mer. 29 avr. 2026 à 23:48
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `agrimans`
--

-- --------------------------------------------------------

--
-- Structure de la table `animal`
--

CREATE TABLE `animal` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `espece` varchar(255) NOT NULL,
  `race` varchar(255) DEFAULT NULL,
  `poids` double DEFAULT NULL,
  `etatSante` varchar(255) DEFAULT NULL,
  `userId` int(11) DEFAULT NULL,
  `date_naissance` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `animal_nourriture`
--

CREATE TABLE `animal_nourriture` (
  `id` int(11) NOT NULL,
  `quantity_fed` double NOT NULL,
  `feeding_date` datetime DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `animal_id` int(11) DEFAULT NULL,
  `nourriture_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `bundle_products`
--

CREATE TABLE `bundle_products` (
  `bundle_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `carts`
--

CREATE TABLE `carts` (
  `id` int(11) NOT NULL,
  `buyer_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `cart_items`
--

CREATE TABLE `cart_items` (
  `id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `cart_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `culture`
--

CREATE TABLE `culture` (
  `id_culture` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `type_culture` varchar(255) DEFAULT NULL,
  `date_plantation` date DEFAULT NULL,
  `date_recolte_prevue` date DEFAULT NULL,
  `etat_culture` varchar(255) DEFAULT NULL,
  `info_file_name` varchar(255) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `parcelle_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `email_otp`
--

CREATE TABLE `email_otp` (
  `id` int(11) NOT NULL,
  `email` varchar(100) NOT NULL,
  `code` varchar(10) NOT NULL,
  `expiry` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `email_otp`
--

INSERT INTO `email_otp` (`id`, `email`, `code`, `expiry`) VALUES
(1, 'user@agrimans.com', '150878', '2026-04-29 18:23:21'),
(2, 'user@agrimans.com', '430839', '2026-04-29 18:23:34'),
(3, 'd.moham2004@gmail.com', '627543', '2026-04-29 18:28:21');

-- --------------------------------------------------------

--
-- Structure de la table `equipement`
--

CREATE TABLE `equipement` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `prix` double DEFAULT NULL,
  `disponibilite` varchar(255) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `equipement`
--

INSERT INTO `equipement` (`id`, `nom`, `type`, `prix`, `disponibilite`, `user_id`) VALUES
(1, 'landini', 'tracteur', 50000, 'Disponible', NULL),
(2, 'FY-12', 'Capteur humidité', 10, 'Disponible', 1),
(3, 'Chevrolet', 'Trax', 100000, 'En maintenance', 1),
(4, 'EF-1103', 'Semoir', 1000, 'Indisponible', 1);

-- --------------------------------------------------------

--
-- Structure de la table `equipement_geo`
--

CREATE TABLE `equipement_geo` (
  `garage_id` int(11) DEFAULT NULL,
  `position_gps` varchar(50) DEFAULT NULL,
  `statut_garage` varchar(20) DEFAULT 'DANS_GARAGE',
  `derniere_localisation` datetime DEFAULT NULL,
  `equipement_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `garage`
--

CREATE TABLE `garage` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `capacite` int(11) DEFAULT NULL,
  `responsable` varchar(255) DEFAULT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  `date_creation` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `garage`
--

INSERT INTO `garage` (`id`, `nom`, `adresse`, `latitude`, `longitude`, `capacite`, `responsable`, `telephone`, `date_creation`) VALUES
(1, 'Agrios', 'Ariana', 36.8625, 10.1956, 12, 'mohamed', '51037288', '2026-04-29 21:30:42'),
(2, 'Agriments', 'Riadh', 24.6833, 46.7333, 20, 'Aziz', '51037288', '2026-04-29 21:33:55'),
(3, 'Agrimans', 'Les calottes glaciaires polaires', 63, 23, 10, 'Nassim', '51037288', '2026-04-29 21:48:44'),
(4, 'Garage Londres (Pluie test)', 'London, UK', 51.5074, -0.1278, 50, 'John Doe', '+44 20 7946 0958', '2026-04-29 22:59:37'),
(5, 'Garage Nuuk (Neige test)', 'Nuuk, Greenland', 64.1814, -51.6941, 20, 'Erik Red', '+299 32 10 00', '2026-04-29 22:59:37'),
(6, 'Garage Dubai (Chaleur test)', 'Dubai, UAE', 25.2048, 55.2708, 100, 'Ahmed Ali', '+971 4 332 2222', '2026-04-29 22:59:37'),
(7, 'Garage Tunis (Météo Idéale)', 'Tunis, Tunisia', 36.8065, 10.1815, 150, 'Mohamed Ben', '+216 71 123 456', '2026-04-29 22:59:37');

-- --------------------------------------------------------

--
-- Structure de la table `garage_equipement`
--

CREATE TABLE `garage_equipement` (
  `garage_id` int(11) NOT NULL,
  `equipement_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `garage_equipement`
--

INSERT INTO `garage_equipement` (`garage_id`, `equipement_id`) VALUES
(1, 2),
(1, 3),
(2, 1),
(2, 3),
(3, 1),
(3, 2),
(3, 3);

-- --------------------------------------------------------

--
-- Structure de la table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` int(11) NOT NULL,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL,
  `available_at` datetime NOT NULL,
  `delivered_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `nourriture`
--

CREATE TABLE `nourriture` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `quantity` double NOT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `nutritional_value` varchar(255) DEFAULT NULL,
  `expiry_date` datetime DEFAULT NULL,
  `supplier` varchar(255) DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `date_added` datetime DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `order`
--

CREATE TABLE `order` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `total_amount` double NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `order_date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `order_item`
--

CREATE TABLE `order_item` (
  `id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` double NOT NULL,
  `price_at_purchase` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `parcelle`
--

CREATE TABLE `parcelle` (
  `id_parcelle` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `superficie` double NOT NULL,
  `localisation` varchar(255) DEFAULT NULL,
  `type_sol` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `utilisateur_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `password_reset`
--

CREATE TABLE `password_reset` (
  `id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `code` int(11) NOT NULL,
  `expiry` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `products`
--

CREATE TABLE `products` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `price` double NOT NULL,
  `quantity` int(11) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `category` varchar(255) NOT NULL,
  `supplier` varchar(255) DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `seller_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `product_bundles`
--

CREATE TABLE `product_bundles` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `original_price` double NOT NULL,
  `bundle_price` double NOT NULL,
  `discount_percentage` double NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `ratings`
--

CREATE TABLE `ratings` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `rating` int(11) NOT NULL,
  `comment` longtext DEFAULT NULL,
  `price_category` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `product_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `rating_likes`
--

CREATE TABLE `rating_likes` (
  `id` int(11) NOT NULL,
  `is_like` tinyint(1) NOT NULL,
  `rating_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `review`
--

CREATE TABLE `review` (
  `id` int(11) NOT NULL,
  `commentaire` longtext DEFAULT NULL,
  `note` int(11) DEFAULT NULL,
  `date_review` date DEFAULT NULL,
  `equipement_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `review`
--

INSERT INTO `review` (`id`, `commentaire`, `note`, `date_review`, `equipement_id`, `user_id`) VALUES
(1, 'tttttttttttttt mauvaise ,,,,,,', 1, '2026-04-29', 2, 1),
(2, 'nnnnnnnnnnn lent jjjjjjjj', 1, '2026-04-29', 2, 1),
(3, 'kkkkkk bad iiiiiiiiii', 1, '2026-04-29', 2, 1),
(4, 'excellent equipement', 5, '2026-04-29', 4, 1),
(5, 'behi barcha', 4, '2026-04-29', 4, 1),
(6, 'tayara', 4, '2026-04-29', 4, 1),
(7, 'y3adi ro7ou', 3, '2026-04-29', 4, 1);

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `face_descriptor` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`face_descriptor`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`id`, `full_name`, `email`, `phone`, `password_hash`, `role`, `created_at`, `face_descriptor`) VALUES
(1, 'Mohamed Daoudi', 'd.moham2004@gmail.com', '51037288', '$2y$13$bN6wcYvVobSVQcHa.6hlyeY9sgfWVaghqE71Vb3JFcr4hh5seMqkS', 'USER', '2026-04-29 21:02:57', '[-0.11702492833137512,0.05277866870164871,0.057425595819950104,-0.027952613309025764,-0.047021202743053436,-0.03757854178547859,-0.08171334862709045,0.03881077095866203,0.13421759009361267,-0.11742165684700012,0.21281464397907257,-0.0028696481604129076,-0.2167411744594574,0.11120323091745377,-0.06655845791101456,0.0497681088745594,-0.14279590547084808,0.006465339567512274,-0.10033538192510605,-0.08943405002355576,0.07637104392051697,0.05730663985013962,0.026563072577118874,0.1137671247124672,-0.1173151284456253,-0.3264647424221039,-0.08647339046001434,-0.15209364891052246,0.0931730717420578,-0.19783267378807068,-0.00027795968344435096,-0.04083545133471489,-0.09256868809461594,0.014537415467202663,-0.016404766589403152,0.04227529466152191,-0.022193241864442825,-0.1119987815618515,0.23284636437892914,0.032004307955503464,-0.11240661144256592,-0.012296251952648163,0.01340709999203682,0.27965009212493896,0.11449568718671799,0.06457757949829102,0.03593697026371956,-0.08657092601060867,0.15155108273029327,-0.23599565029144287,0.1068389043211937,0.11759653687477112,0.08099532872438431,0.11838125437498093,0.12463237345218658,-0.17459535598754883,0.09229301661252975,0.09608399122953415,-0.15605312585830688,0.11829646676778793,0.005363684147596359,0.011406753212213516,0.026799989864230156,0.0285131074488163,0.1403735727071762,0.08649100363254547,-0.06943422555923462,-0.13099002838134766,0.07628879696130753,-0.22038422524929047,0.004079088568687439,0.153007373213768,-0.08235184848308563,-0.19854071736335754,-0.1434127539396286,0.05758047103881836,0.37078413367271423,0.22178424894809723,-0.11320820450782776,0.02456759102642536,-0.06189991906285286,-0.13772127032279968,0.11130761355161667,0.0643187090754509,-0.10946189612150192,-0.041972868144512177,-0.07247953116893768,0.10032057762145996,0.19535653293132782,0.021986408159136772,-0.006887110415846109,0.2053316980600357,0.022821709513664246,0.00565221207216382,0.025400705635547638,0.026741642504930496,-0.2115871012210846,0.013680430129170418,-0.028140798211097717,-0.025173218920826912,0.08998314291238785,-0.05420365184545517,-0.019505245611071587,0.040177129209041595,-0.2318209409713745,0.1415330171585083,-0.04804600775241852,-0.09060647338628769,0.04174334928393364,0.06126425415277481,-0.15526701509952545,-0.024387642741203308,0.1843499094247818,-0.22204706072807312,0.1550997495651245,0.1793697625398636,0.08129481971263885,0.14088095724582672,0.05525226891040802,0.049682680517435074,0.037364937365055084,-0.016061490401625633,-0.14805690944194794,-0.027512982487678528,0.027407154440879822,-0.013502273708581924,0.00046631894656457007,-0.0709647387266159]'),
(2, 'Test User', 'user@agrimans.com', NULL, '$2y$10$FCa92g9tIsn7MY.ZKfqvm.44vz2Aop4I8Ua.b83mEpE2G05tSyTUO', 'USER', '2026-04-29 21:09:40', '[-0.11702492833137512,0.05277866870164871,0.057425595819950104,-0.027952613309025764,-0.047021202743053436,-0.03757854178547859,-0.08171334862709045,0.03881077095866203,0.13421759009361267,-0.11742165684700012,0.21281464397907257,-0.0028696481604129076,-0.2167411744594574,0.11120323091745377,-0.06655845791101456,0.0497681088745594,-0.14279590547084808,0.006465339567512274,-0.10033538192510605,-0.08943405002355576,0.07637104392051697,0.05730663985013962,0.026563072577118874,0.1137671247124672,-0.1173151284456253,-0.3264647424221039,-0.08647339046001434,-0.15209364891052246,0.0931730717420578,-0.19783267378807068,-0.00027795968344435096,-0.04083545133471489,-0.09256868809461594,0.014537415467202663,-0.016404766589403152,0.04227529466152191,-0.022193241864442825,-0.1119987815618515,0.23284636437892914,0.032004307955503464,-0.11240661144256592,-0.012296251952648163,0.01340709999203682,0.27965009212493896,0.11449568718671799,0.06457757949829102,0.03593697026371956,-0.08657092601060867,0.15155108273029327,-0.23599565029144287,0.1068389043211937,0.11759653687477112,0.08099532872438431,0.11838125437498093,0.12463237345218658,-0.17459535598754883,0.09229301661252975,0.09608399122953415,-0.15605312585830688,0.11829646676778793,0.005363684147596359,0.011406753212213516,0.026799989864230156,0.0285131074488163,0.1403735727071762,0.08649100363254547,-0.06943422555923462,-0.13099002838134766,0.07628879696130753,-0.22038422524929047,0.004079088568687439,0.153007373213768,-0.08235184848308563,-0.19854071736335754,-0.1434127539396286,0.05758047103881836,0.37078413367271423,0.22178424894809723,-0.11320820450782776,0.02456759102642536,-0.06189991906285286,-0.13772127032279968,0.11130761355161667,0.0643187090754509,-0.10946189612150192,-0.041972868144512177,-0.07247953116893768,0.10032057762145996,0.19535653293132782,0.021986408159136772,-0.006887110415846109,0.2053316980600357,0.022821709513664246,0.00565221207216382,0.025400705635547638,0.026741642504930496,-0.2115871012210846,0.013680430129170418,-0.028140798211097717,-0.025173218920826912,0.08998314291238785,-0.05420365184545517,-0.019505245611071587,0.040177129209041595,-0.2318209409713745,0.1415330171585083,-0.04804600775241852,-0.09060647338628769,0.04174334928393364,0.06126425415277481,-0.15526701509952545,-0.024387642741203308,0.1843499094247818,-0.22204706072807312,0.1550997495651245,0.1793697625398636,0.08129481971263885,0.14088095724582672,0.05525226891040802,0.049682680517435074,0.037364937365055084,-0.016061490401625633,-0.14805690944194794,-0.027512982487678528,0.027407154440879822,-0.013502273708581924,0.00046631894656457007,-0.0709647387266159]'),
(3, 'Test Admin', 'admin@agrimans.com', NULL, '$2y$10$Q4.OGGQNLSg.1MARl5vBTuQuTnmxVRzsqx2V8sgYQnWDIGH9sdJB.', 'ADMIN', '2026-04-29 21:09:40', '[-0.11702492833137512,0.05277866870164871,0.057425595819950104,-0.027952613309025764,-0.047021202743053436,-0.03757854178547859,-0.08171334862709045,0.03881077095866203,0.13421759009361267,-0.11742165684700012,0.21281464397907257,-0.0028696481604129076,-0.2167411744594574,0.11120323091745377,-0.06655845791101456,0.0497681088745594,-0.14279590547084808,0.006465339567512274,-0.10033538192510605,-0.08943405002355576,0.07637104392051697,0.05730663985013962,0.026563072577118874,0.1137671247124672,-0.1173151284456253,-0.3264647424221039,-0.08647339046001434,-0.15209364891052246,0.0931730717420578,-0.19783267378807068,-0.00027795968344435096,-0.04083545133471489,-0.09256868809461594,0.014537415467202663,-0.016404766589403152,0.04227529466152191,-0.022193241864442825,-0.1119987815618515,0.23284636437892914,0.032004307955503464,-0.11240661144256592,-0.012296251952648163,0.01340709999203682,0.27965009212493896,0.11449568718671799,0.06457757949829102,0.03593697026371956,-0.08657092601060867,0.15155108273029327,-0.23599565029144287,0.1068389043211937,0.11759653687477112,0.08099532872438431,0.11838125437498093,0.12463237345218658,-0.17459535598754883,0.09229301661252975,0.09608399122953415,-0.15605312585830688,0.11829646676778793,0.005363684147596359,0.011406753212213516,0.026799989864230156,0.0285131074488163,0.1403735727071762,0.08649100363254547,-0.06943422555923462,-0.13099002838134766,0.07628879696130753,-0.22038422524929047,0.004079088568687439,0.153007373213768,-0.08235184848308563,-0.19854071736335754,-0.1434127539396286,0.05758047103881836,0.37078413367271423,0.22178424894809723,-0.11320820450782776,0.02456759102642536,-0.06189991906285286,-0.13772127032279968,0.11130761355161667,0.0643187090754509,-0.10946189612150192,-0.041972868144512177,-0.07247953116893768,0.10032057762145996,0.19535653293132782,0.021986408159136772,-0.006887110415846109,0.2053316980600357,0.022821709513664246,0.00565221207216382,0.025400705635547638,0.026741642504930496,-0.2115871012210846,0.013680430129170418,-0.028140798211097717,-0.025173218920826912,0.08998314291238785,-0.05420365184545517,-0.019505245611071587,0.040177129209041595,-0.2318209409713745,0.1415330171585083,-0.04804600775241852,-0.09060647338628769,0.04174334928393364,0.06126425415277481,-0.15526701509952545,-0.024387642741203308,0.1843499094247818,-0.22204706072807312,0.1550997495651245,0.1793697625398636,0.08129481971263885,0.14088095724582672,0.05525226891040802,0.049682680517435074,0.037364937365055084,-0.016061490401625633,-0.14805690944194794,-0.027512982487678528,0.027407154440879822,-0.013502273708581924,0.00046631894656457007,-0.0709647387266159]');

-- --------------------------------------------------------

--
-- Structure de la table `user_otp`
--

CREATE TABLE `user_otp` (
  `user_id` int(11) NOT NULL,
  `otp_code` varchar(255) NOT NULL,
  `expires_at` datetime NOT NULL,
  `attempts` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `animal`
--
ALTER TABLE `animal`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `animal_nourriture`
--
ALTER TABLE `animal_nourriture`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_A9B23EF78E962C16` (`animal_id`),
  ADD KEY `IDX_A9B23EF798BD5834` (`nourriture_id`);

--
-- Index pour la table `bundle_products`
--
ALTER TABLE `bundle_products`
  ADD PRIMARY KEY (`bundle_id`,`product_id`),
  ADD KEY `IDX_9A956B7BF1FAD9D3` (`bundle_id`),
  ADD KEY `IDX_9A956B7B4584665A` (`product_id`);

--
-- Index pour la table `carts`
--
ALTER TABLE `carts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_4E004AAC6C755722` (`buyer_id`);

--
-- Index pour la table `cart_items`
--
ALTER TABLE `cart_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_BEF484451AD5CDBF` (`cart_id`),
  ADD KEY `IDX_BEF484454584665A` (`product_id`);

--
-- Index pour la table `culture`
--
ALTER TABLE `culture`
  ADD PRIMARY KEY (`id_culture`),
  ADD KEY `IDX_B6A99CEB4433ED66` (`parcelle_id`);

--
-- Index pour la table `email_otp`
--
ALTER TABLE `email_otp`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `equipement`
--
ALTER TABLE `equipement`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_B8B4C6F3A76ED395` (`user_id`);

--
-- Index pour la table `equipement_geo`
--
ALTER TABLE `equipement_geo`
  ADD PRIMARY KEY (`equipement_id`);

--
-- Index pour la table `garage`
--
ALTER TABLE `garage`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `garage_equipement`
--
ALTER TABLE `garage_equipement`
  ADD PRIMARY KEY (`garage_id`,`equipement_id`),
  ADD KEY `IDX_C1B083F5C4FFF555` (`garage_id`),
  ADD KEY `IDX_C1B083F5806F0F5C` (`equipement_id`);

--
-- Index pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `nourriture`
--
ALTER TABLE `nourriture`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `order`
--
ALTER TABLE `order`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `order_item`
--
ALTER TABLE `order_item`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `parcelle`
--
ALTER TABLE `parcelle`
  ADD PRIMARY KEY (`id_parcelle`),
  ADD KEY `IDX_C56E2CF6FB88E14F` (`utilisateur_id`);

--
-- Index pour la table `password_reset`
--
ALTER TABLE `password_reset`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_B3BA5A5A8DE820D9` (`seller_id`);

--
-- Index pour la table `product_bundles`
--
ALTER TABLE `product_bundles`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `ratings`
--
ALTER TABLE `ratings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_CEB607C94584665A` (`product_id`),
  ADD KEY `IDX_CEB607C9A76ED395` (`user_id`);

--
-- Index pour la table `rating_likes`
--
ALTER TABLE `rating_likes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_D4DC4141A32EFC6` (`rating_id`),
  ADD KEY `IDX_D4DC4141A76ED395` (`user_id`);

--
-- Index pour la table `review`
--
ALTER TABLE `review`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_794381C6806F0F5C` (`equipement_id`),
  ADD KEY `IDX_794381C6A76ED395` (`user_id`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_8D93D649E7927C74` (`email`);

--
-- Index pour la table `user_otp`
--
ALTER TABLE `user_otp`
  ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `animal`
--
ALTER TABLE `animal`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `animal_nourriture`
--
ALTER TABLE `animal_nourriture`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `carts`
--
ALTER TABLE `carts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `cart_items`
--
ALTER TABLE `cart_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `culture`
--
ALTER TABLE `culture`
  MODIFY `id_culture` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `email_otp`
--
ALTER TABLE `email_otp`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `equipement`
--
ALTER TABLE `equipement`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `garage`
--
ALTER TABLE `garage`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `nourriture`
--
ALTER TABLE `nourriture`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `order`
--
ALTER TABLE `order`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `order_item`
--
ALTER TABLE `order_item`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `parcelle`
--
ALTER TABLE `parcelle`
  MODIFY `id_parcelle` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `password_reset`
--
ALTER TABLE `password_reset`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `products`
--
ALTER TABLE `products`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `product_bundles`
--
ALTER TABLE `product_bundles`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `ratings`
--
ALTER TABLE `ratings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `rating_likes`
--
ALTER TABLE `rating_likes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `review`
--
ALTER TABLE `review`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `user_otp`
--
ALTER TABLE `user_otp`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `animal_nourriture`
--
ALTER TABLE `animal_nourriture`
  ADD CONSTRAINT `FK_A9B23EF78E962C16` FOREIGN KEY (`animal_id`) REFERENCES `animal` (`id`),
  ADD CONSTRAINT `FK_A9B23EF798BD5834` FOREIGN KEY (`nourriture_id`) REFERENCES `nourriture` (`id`);

--
-- Contraintes pour la table `bundle_products`
--
ALTER TABLE `bundle_products`
  ADD CONSTRAINT `FK_9A956B7B4584665A` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `FK_9A956B7BF1FAD9D3` FOREIGN KEY (`bundle_id`) REFERENCES `product_bundles` (`id`);

--
-- Contraintes pour la table `carts`
--
ALTER TABLE `carts`
  ADD CONSTRAINT `FK_4E004AAC6C755722` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `cart_items`
--
ALTER TABLE `cart_items`
  ADD CONSTRAINT `FK_BEF484451AD5CDBF` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_BEF484454584665A` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `culture`
--
ALTER TABLE `culture`
  ADD CONSTRAINT `FK_B6A99CEB4433ED66` FOREIGN KEY (`parcelle_id`) REFERENCES `parcelle` (`id_parcelle`) ON DELETE SET NULL;

--
-- Contraintes pour la table `equipement`
--
ALTER TABLE `equipement`
  ADD CONSTRAINT `FK_B8B4C6F3A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `equipement_geo`
--
ALTER TABLE `equipement_geo`
  ADD CONSTRAINT `FK_7ED7FDDF806F0F5C` FOREIGN KEY (`equipement_id`) REFERENCES `equipement` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `garage_equipement`
--
ALTER TABLE `garage_equipement`
  ADD CONSTRAINT `FK_C1B083F5806F0F5C` FOREIGN KEY (`equipement_id`) REFERENCES `equipement` (`id`),
  ADD CONSTRAINT `FK_C1B083F5C4FFF555` FOREIGN KEY (`garage_id`) REFERENCES `garage` (`id`);

--
-- Contraintes pour la table `parcelle`
--
ALTER TABLE `parcelle`
  ADD CONSTRAINT `FK_C56E2CF6FB88E14F` FOREIGN KEY (`utilisateur_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `FK_B3BA5A5A8DE820D9` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `ratings`
--
ALTER TABLE `ratings`
  ADD CONSTRAINT `FK_CEB607C94584665A` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `FK_CEB607C9A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `rating_likes`
--
ALTER TABLE `rating_likes`
  ADD CONSTRAINT `FK_D4DC4141A32EFC6` FOREIGN KEY (`rating_id`) REFERENCES `ratings` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_D4DC4141A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `review`
--
ALTER TABLE `review`
  ADD CONSTRAINT `FK_794381C6806F0F5C` FOREIGN KEY (`equipement_id`) REFERENCES `equipement` (`id`),
  ADD CONSTRAINT `FK_794381C6A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
