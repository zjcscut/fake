CREATE TABLE `t_user` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(30) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `birth` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;