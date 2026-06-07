-- =====================================================================
--  BikeShare — Schéma de base de données (MySQL / MariaDB)
--  Plateforme de location de vélos entre particuliers
--  Base de données BikeShare — Yassin Iallalen, ICC Bruxelles
--  Encodage : utf8mb4 / utf8mb4_general_ci
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `bikeshare`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `bikeshare`;

-- ---------------------------------------------------------------------
--  Rôles & utilisateurs
-- ---------------------------------------------------------------------
CREATE TABLE `roles` (
  `id`   BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `role` VARCHAR(255) DEFAULT NULL,        -- ROLE_VISITOR, ROLE_MEMBER, ROLE_ADMIN
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `users` (
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `last_name`     VARCHAR(255) NOT NULL,
  `first_name`    VARCHAR(255) NOT NULL,
  `email`         VARCHAR(255) NOT NULL,
  `adresse`       VARCHAR(255) NOT NULL,
  `postal_code`   VARCHAR(10)  NOT NULL,
  `locality`      VARCHAR(255) NOT NULL,
  `phone`         VARCHAR(20)  DEFAULT NULL,
  `password`      VARCHAR(255) NOT NULL,
  `profil_picture` VARCHAR(255) DEFAULT NULL,
  `iban`          VARCHAR(255) DEFAULT NULL,
  `bic`           VARCHAR(255) DEFAULT NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_verified`   BIT(1)       NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `user_role` (
  `id`      BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL,
  `role_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_role_user` (`user_id`),
  KEY `fk_user_role_role` (`role_id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Documents d'identité / vérification de profil
CREATE TABLE `documents` (
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `document_type` VARCHAR(255) DEFAULT NULL, -- ID_CARD, PROOF_OF_ADDRESS...
  `file_path`     VARCHAR(255) DEFAULT NULL,
  `user_id`       BIGINT(20)   NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_documents_user` (`user_id`),
  CONSTRAINT `fk_documents_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
--  Catégories, tarifs, vélos
-- ---------------------------------------------------------------------
-- Catégories : Ville, VTT, Route, Électrique, Pliant, Cargo, Enfant...
CREATE TABLE `categories` (
  `id`       BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `category` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `prices` (
  `id`           BIGINT(20) NOT NULL AUTO_INCREMENT,
  `high_price`   DOUBLE DEFAULT NULL,   -- tarif haute saison (€/jour)
  `low_price`    DOUBLE DEFAULT NULL,   -- tarif basse saison
  `middle_price` DOUBLE DEFAULT NULL,   -- tarif moyenne saison
  `promo_1`      DOUBLE DEFAULT NULL,   -- réduction location ≥ 3 jours (%)
  `promo_2`      DOUBLE DEFAULT NULL,   -- réduction location ≥ 7 jours (%)
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Table centrale : un vélo proposé à la location (équivalent de `cars`)
CREATE TABLE `bikes` (
  `id`                BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `brand`             VARCHAR(255) DEFAULT NULL,  -- ex. Btwin, Trek, Cube
  `model`             VARCHAR(255) DEFAULT NULL,
  `adresse`           VARCHAR(255) DEFAULT NULL,
  `postal_code`       VARCHAR(255) DEFAULT NULL,
  `locality`          VARCHAR(255) DEFAULT NULL,
  `frame_number`      VARCHAR(255) DEFAULT NULL,  -- numéro de cadre
  `purchase_year`     DATE         DEFAULT NULL,  -- année d'achat
  `offer`             VARCHAR(255) DEFAULT NULL,  -- type d'offre
  `reservation_mode`  VARCHAR(255) DEFAULT NULL,  -- INSTANT / ON_REQUEST
  `bike_type`         VARCHAR(255) DEFAULT NULL,  -- CITY, MTB, ROAD, ELECTRIC...
  `wheel_size`        VARCHAR(255) DEFAULT NULL,  -- 26", 28", 29"
  `gears`             INT(11)      DEFAULT NULL,  -- nombre de vitesses
  `is_electric`       BIT(1)       DEFAULT b'0',
  `frame_size`        VARCHAR(255) DEFAULT NULL,  -- S, M, L, XL
  `registration_path` VARCHAR(255) DEFAULT NULL,  -- preuve d'achat / gravure
  `category_id`       BIGINT(20)   DEFAULT NULL,
  `user_id`           BIGINT(20)   DEFAULT NULL,  -- propriétaire
  `latitude`          DOUBLE       DEFAULT NULL,
  `longitude`         DOUBLE       DEFAULT NULL,
  `online`            BIT(1)       DEFAULT b'1',
  `price_id`          BIGINT(20)   DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_bikes_category` (`category_id`),
  KEY `fk_bikes_user` (`user_id`),
  KEY `fk_bikes_price` (`price_id`),
  CONSTRAINT `fk_bikes_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `fk_bikes_user`     FOREIGN KEY (`user_id`)     REFERENCES `users` (`id`),
  CONSTRAINT `fk_bikes_price`    FOREIGN KEY (`price_id`)    REFERENCES `prices` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Photos du vélo
CREATE TABLE `pictures` (
  `id`      BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `url`     VARCHAR(255) DEFAULT NULL,
  `bike_id` BIGINT(20)   DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pictures_bike` (`bike_id`),
  CONSTRAINT `fk_pictures_bike` FOREIGN KEY (`bike_id`) REFERENCES `bikes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Conditions de location fixées par le propriétaire
CREATE TABLE `conditions` (
  `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(255) DEFAULT NULL,
  `bike_id`     BIGINT(20)   DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_conditions_bike` (`bike_id`),
  CONSTRAINT `fk_conditions_bike` FOREIGN KEY (`bike_id`) REFERENCES `bikes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
--  Équipements & caractéristiques (relations N-N)
-- ---------------------------------------------------------------------
-- Équipements : antivol, casque, panier, porte-bagage, éclairage...
CREATE TABLE `equipements` (
  `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(255) DEFAULT NULL,
  `icon`        VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `bike_equipment` (
  `id`           BIGINT(20) NOT NULL AUTO_INCREMENT,
  `bike_id`      BIGINT(20) NOT NULL,
  `equipment_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_be_bike` (`bike_id`),
  KEY `fk_be_equip` (`equipment_id`),
  CONSTRAINT `fk_be_bike`  FOREIGN KEY (`bike_id`)      REFERENCES `bikes` (`id`),
  CONSTRAINT `fk_be_equip` FOREIGN KEY (`equipment_id`) REFERENCES `equipements` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Caractéristiques : freins à disque, assistance électrique, suspension...
CREATE TABLE `features` (
  `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(255) DEFAULT NULL,
  `nom`         VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `bike_feature` (
  `id`         BIGINT(20) NOT NULL AUTO_INCREMENT,
  `bike_id`    BIGINT(20) NOT NULL,
  `feature_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_bf_bike` (`bike_id`),
  KEY `fk_bf_feature` (`feature_id`),
  CONSTRAINT `fk_bf_bike`    FOREIGN KEY (`bike_id`)    REFERENCES `bikes` (`id`),
  CONSTRAINT `fk_bf_feature` FOREIGN KEY (`feature_id`) REFERENCES `features` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Indisponibilités (calendrier du propriétaire)
CREATE TABLE `unavailabilities` (
  `id`         BIGINT(20) NOT NULL AUTO_INCREMENT,
  `start_date` DATE       NOT NULL,
  `end_date`   DATE       NOT NULL,
  `bike_id`    BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_unavail_bike` (`bike_id`),
  CONSTRAINT `fk_unavail_bike` FOREIGN KEY (`bike_id`) REFERENCES `bikes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
--  Réservations & paiements
-- ---------------------------------------------------------------------
CREATE TABLE `reservations` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `created_at`     DATETIME(6)  DEFAULT NULL,
  `start_location` DATE         DEFAULT NULL,   -- date de début
  `end_location`   DATE         DEFAULT NULL,   -- date de fin
  `duration`       INT(11)      DEFAULT NULL,   -- nb de jours
  `statut`         VARCHAR(255) DEFAULT NULL,   -- PENDING, CONFIRMED, CANCELLED, COMPLETED
  `assurance`      VARCHAR(255) DEFAULT NULL,   -- option assurance antivol/casse
  `bike_id`        BIGINT(20)   NOT NULL,
  `user_id`        BIGINT(20)   NOT NULL,       -- locataire
  PRIMARY KEY (`id`),
  KEY `fk_resa_bike` (`bike_id`),
  KEY `fk_resa_user` (`user_id`),
  CONSTRAINT `fk_resa_bike` FOREIGN KEY (`bike_id`) REFERENCES `bikes` (`id`),
  CONSTRAINT `fk_resa_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `payments` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `created_at`     DATETIME     NOT NULL,
  `payment_mode`   VARCHAR(255) NOT NULL,       -- STRIPE, CARD...
  `total_price`    DOUBLE       NOT NULL,
  `statut`         VARCHAR(255) NOT NULL,       -- PAID, REFUNDED, FAILED
  `reservation_id` BIGINT(20)   DEFAULT NULL,
  `part_bikeshare` DOUBLE       NOT NULL,       -- commission plateforme
  PRIMARY KEY (`id`),
  KEY `fk_payments_resa` (`reservation_id`),
  CONSTRAINT `fk_payments_resa` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Gains reversés au propriétaire
CREATE TABLE `gains` (
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `created_at`    DATETIME(6)  NOT NULL,
  `description`   VARCHAR(255) DEFAULT NULL,
  `amount_earned` DOUBLE       NOT NULL,
  `status`        VARCHAR(255) NOT NULL,        -- PENDING, TRANSFERRED
  `payment_id`    BIGINT(20)   NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_gains_payment` (`payment_id`),
  CONSTRAINT `fk_gains_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Remboursements
CREATE TABLE `refunds` (
  `id`                BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `amount`            DOUBLE      NOT NULL,
  `created_at`        DATETIME(6) NOT NULL,
  `refund_percentage` DOUBLE      NOT NULL,
  `payment_id`        BIGINT(20)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_refunds_payment` (`payment_id`),
  CONSTRAINT `fk_refunds_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ---------------------------------------------------------------------
--  Communication, évaluations, réclamations, notifications
-- ---------------------------------------------------------------------
CREATE TABLE `chat_messages` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `content`        VARCHAR(255) NOT NULL,
  `reservation_id` BIGINT(20)   NOT NULL,
  `sent_at`        DATETIME(6)  NOT NULL,
  `from_user_id`   BIGINT(20)   NOT NULL,
  `to_user_id`     BIGINT(20)   NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_chat_resa` (`reservation_id`),
  KEY `fk_chat_from` (`from_user_id`),
  KEY `fk_chat_to`   (`to_user_id`),
  CONSTRAINT `fk_chat_resa` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`),
  CONSTRAINT `fk_chat_from` FOREIGN KEY (`from_user_id`)   REFERENCES `users` (`id`),
  CONSTRAINT `fk_chat_to`   FOREIGN KEY (`to_user_id`)     REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `evaluations` (
  `id`             BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `comment`        TEXT        DEFAULT NULL,
  `created_at`     DATETIME(6) NOT NULL,
  `note`           INT(11)     NOT NULL,         -- 1 à 5
  `reservation_id` BIGINT(20)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_eval_resa` (`reservation_id`),
  CONSTRAINT `fk_eval_resa` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `claims` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `created_at`     DATETIME(6)  NOT NULL,
  `message`        VARCHAR(255) NOT NULL,
  `status`         VARCHAR(255) NOT NULL,        -- OPEN, IN_REVIEW, RESOLVED
  `reservation_id` BIGINT(20)   NOT NULL,
  `claimant_role`  VARCHAR(255) NOT NULL,        -- OWNER / RENTER
  `response`       VARCHAR(255) DEFAULT NULL,
  `response_at`    DATETIME(6)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_claims_resa` (`reservation_id`),
  CONSTRAINT `fk_claims_resa` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `notifications` (
  `id`           BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `created_at`   DATETIME(6)  DEFAULT NULL,
  `message`      VARCHAR(255) DEFAULT NULL,
  `type`         VARCHAR(255) NOT NULL,
  `bike_id`      BIGINT(20)   NOT NULL,
  `from_user_id` BIGINT(20)   NOT NULL,
  `to_user_id`   BIGINT(20)   NOT NULL,
  `is_read`      BIT(1)       DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `fk_notif_bike` (`bike_id`),
  KEY `fk_notif_from` (`from_user_id`),
  KEY `fk_notif_to`   (`to_user_id`),
  CONSTRAINT `fk_notif_bike` FOREIGN KEY (`bike_id`)      REFERENCES `bikes` (`id`),
  CONSTRAINT `fk_notif_from` FOREIGN KEY (`from_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_notif_to`   FOREIGN KEY (`to_user_id`)   REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =====================================================================
--  Données de référence
-- =====================================================================
INSERT INTO `roles` (`id`,`role`) VALUES
  (1,'ROLE_VISITOR'), (2,'ROLE_MEMBER'), (3,'ROLE_ADMIN');

INSERT INTO `categories` (`id`,`category`) VALUES
  (1,'Vélo de ville'), (2,'VTT'), (3,'Vélo de route'),
  (4,'Vélo électrique'), (5,'Vélo pliant'), (6,'Vélo cargo'),
  (7,'Vélo enfant');

INSERT INTO `equipements` (`id`,`description`,`icon`) VALUES
  (1,'Antivol U','bi-lock'),
  (2,'Casque','bi-shield'),
  (3,'Panier','bi-basket'),
  (4,'Porte-bagage','bi-bag'),
  (5,'Éclairage LED','bi-lightbulb'),
  (6,'Sacoche','bi-backpack'),
  (7,'Pompe','bi-wind'),
  (8,'Siège enfant','bi-person-arms-up');

INSERT INTO `features` (`id`,`nom`,`description`) VALUES
  (1,'Freins à disque','Freinage hydraulique toutes conditions'),
  (2,'Assistance électrique','Moteur central, autonomie 60 km'),
  (3,'Suspension avant','Fourche télescopique 100 mm'),
  (4,'Transmission Shimano','Dérailleur 21 vitesses'),
  (5,'Cadre aluminium','Léger et résistant à la corrosion'),
  (6,'Pneus anti-crevaison','Bandes de protection renforcées');

SET FOREIGN_KEY_CHECKS = 1;
-- =====================================================================
--  Fin du schéma BikeShare
-- =====================================================================
