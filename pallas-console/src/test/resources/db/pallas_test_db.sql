
CREATE SCHEMA `pallas_test_example_db` ;

CREATE TABLE IF NOT EXISTS `pallas_test_example_db`.`pallas_test_example_table` (
  `id` BIGINT(64) NOT NULL AUTO_INCREMENT,
  `token` VARCHAR(32) NOT NULL DEFAULT 'token',
  `status` TINYINT(4) NOT NULL DEFAULT 0,
  `create_time` TIMESTAMP NOT NULL DEFAULT '1970-01-02 00:00:00',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
 );