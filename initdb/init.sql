CREATE DATABASE IF NOT EXISTS fuel_statistics_db;
USE fuel_statistics_db;

CREATE TABLE IF NOT EXISTS `user` (
                        `id` bigint NOT NULL,
                        `name` varchar(100) NOT NULL,
                        `state` varchar(100) NOT NULL,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `statistics_data` (
                                   `id` int NOT NULL AUTO_INCREMENT,
                                   `start_date` date DEFAULT NULL,
                                   `end_date` date DEFAULT NULL,
                                   `user_id` bigint DEFAULT NULL,
                                   PRIMARY KEY (`id`),
                                   KEY `statisticsData_user_fk` (`user_id`),
                                   CONSTRAINT `statisticsData_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;