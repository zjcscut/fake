CREATE TABLE `t_user` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(30) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `birth` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE uniq_name(`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

INSERT INTO t_user(name,birth) VALUES ('doge','2017-11-23 23:23:23');