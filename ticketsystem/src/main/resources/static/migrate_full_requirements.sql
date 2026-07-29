-- MySQL 8.0 compatible, repeatable migration. File content is ASCII-only on purpose.
USE `ticket_system`;

DELIMITER $$

DROP PROCEDURE IF EXISTS `add_column_if_missing`$$
CREATE PROCEDURE `add_column_if_missing`(
    IN target_table VARCHAR(64),
    IN target_column VARCHAR(64),
    IN column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = target_table
          AND column_name = target_column
    ) THEN
        SET @migration_sql = CONCAT('ALTER TABLE `', target_table, '` ADD COLUMN `',
                                    target_column, '` ', column_definition);
        PREPARE migration_stmt FROM @migration_sql;
        EXECUTE migration_stmt;
        DEALLOCATE PREPARE migration_stmt;
    END IF;
END$$

CALL add_column_if_missing('sys_user', 'agent_group_id', 'BIGINT DEFAULT NULL AFTER `reputation_score`')$$
CALL add_column_if_missing('sys_user', 'last_login_time', 'DATETIME DEFAULT NULL AFTER `agent_group_id`')$$
CALL add_column_if_missing('ticket', 'group_id', 'BIGINT DEFAULT NULL AFTER `agent_id`')$$
CALL add_column_if_missing('ticket', 'archived', 'TINYINT NOT NULL DEFAULT 0 AFTER `close_time`')$$
CALL add_column_if_missing('ticket', 'archive_time', 'DATETIME DEFAULT NULL AFTER `archived`')$$

DROP PROCEDURE `add_column_if_missing`$$
DELIMITER ;

CREATE TABLE IF NOT EXISTS `agent_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `group_name` VARCHAR(80) NOT NULL,
    `leader_id` BIGINT DEFAULT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ticket_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_code` VARCHAR(30) NOT NULL,
    `category_name` VARCHAR(80) NOT NULL,
    `group_id` BIGINT DEFAULT NULL,
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- UTF-8 labels are stored as hexadecimal literals so Windows mysql source encoding cannot corrupt them.
INSERT INTO `ticket_category` (`category_code`, `category_name`, `enabled`, `deleted`)
VALUES
    ('REFUND',    CONVERT(0xE98080E6ACBEE98080E8B4A7 USING utf8mb4), 1, 0),
    ('LOGISTICS', CONVERT(0xE789A9E6B581E5BC82E5B8B8 USING utf8mb4), 1, 0),
    ('DAMAGE',    CONVERT(0xE59586E59381E7A0B4E68D9F USING utf8mb4), 1, 0),
    ('INVOICE',   CONVERT(0xE58F91E7A5A8E997AEE9A298 USING utf8mb4), 1, 0),
    ('OTHER',     CONVERT(0xE585B6E4BB96E997AEE9A298 USING utf8mb4), 1, 0)
ON DUPLICATE KEY UPDATE `category_name` = VALUES(`category_name`), `deleted` = 0;

CREATE TABLE IF NOT EXISTS `ticket_satisfaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ticket_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `score` TINYINT NOT NULL,
    `comment` VARCHAR(1000) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_satisfaction_ticket_user` (`ticket_id`, `user_id`),
    KEY `idx_satisfaction_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_no` VARCHAR(64) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_chat_session_no` (`session_no`),
    KEY `idx_ai_chat_session_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` BIGINT NOT NULL,
    `sender_type` VARCHAR(10) NOT NULL,
    `content` TEXT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_ai_chat_message_session` (`session_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `faq_semantic_config` (
    `id` BIGINT NOT NULL,
    `enabled` TINYINT NOT NULL DEFAULT 0,
    `similarity_threshold` DECIMAL(4,3) NOT NULL DEFAULT 0.650,
    `max_candidates` INT NOT NULL DEFAULT 30,
    `max_results` INT NOT NULL DEFAULT 5,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `faq_semantic_config`
    (`id`, `enabled`, `similarity_threshold`, `max_candidates`, `max_results`)
VALUES (1, 0, 0.650, 30, 5)
ON DUPLICATE KEY UPDATE `id` = VALUES(`id`);
