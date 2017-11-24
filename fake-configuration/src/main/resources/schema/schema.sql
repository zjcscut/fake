CREATE TABLE IF NOT EXISTS `t_user` (
  `id` bigint(20) unsigned AUTO_INCREMENT NOT NULL,
  `name` varchar(30) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `birth` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE uniq_name(`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

TRUNCATE t_user;
INSERT INTO t_user(name,birth) VALUES ('doge','2017-11-23 23:23:23');