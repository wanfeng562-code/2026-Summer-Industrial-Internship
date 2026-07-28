-- 成员 C 工单流转增量迁移。
-- 可在成员 A 数据库基线上重复执行，不删除现有业务数据。
USE `ticket_system`;

CREATE TABLE IF NOT EXISTS `ticket_operation_log` (
                                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                                      `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
                                                      `action` VARCHAR(40) NOT NULL COMMENT '动作代码',
                                                      `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID，系统操作为空',
                                                      `operator_role` VARCHAR(20) NOT NULL COMMENT '操作人角色: USER/AGENT/ADMIN/SYSTEM',
                                                      `before_status` VARCHAR(30) DEFAULT NULL COMMENT '变更前状态',
                                                      `after_status` VARCHAR(30) DEFAULT NULL COMMENT '变更后状态',
                                                      `detail` VARCHAR(2000) DEFAULT NULL COMMENT '必要说明',
                                                      `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                                      PRIMARY KEY (`id`),
                                                      KEY `idx_ticket_id` (`ticket_id`),
                                                      KEY `idx_operator_id` (`operator_id`),
                                                      KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单操作日志表';

-- MySQL 不支持所有版本通用的 ADD COLUMN IF NOT EXISTS，使用 information_schema 保证可重复执行。
SET @sla_hours_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'after_sale_policy'
      AND COLUMN_NAME = 'sla_hours'
);
SET @add_sla_hours_sql = IF(
        @sla_hours_exists = 0,
        'ALTER TABLE `after_sale_policy` ADD COLUMN `sla_hours` INT DEFAULT NULL COMMENT ''命中策略后的SLA时长（小时）'' AFTER `enabled`',
        'SELECT 1'
                         );
PREPARE add_sla_hours_stmt FROM @add_sla_hours_sql;
EXECUTE add_sla_hours_stmt;
DEALLOCATE PREPARE add_sla_hours_stmt;

-- 为老师 7-28 的演示策略补充 SLA；只填充空值，不覆盖成员自行配置。
UPDATE `after_sale_policy`
SET `sla_hours` = CASE
                      WHEN `priority` >= 3 THEN 4
                      WHEN `category` IN ('REFUND', 'DAMAGE') THEN 24
                      ELSE 48
    END
WHERE `sla_hours` IS NULL AND `deleted` = 0;

CREATE TABLE IF NOT EXISTS `faq` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
                                    `category` VARCHAR(30) NOT NULL COMMENT '分类',
                                    `question` VARCHAR(500) NOT NULL COMMENT '常见问题',
                                    `answer` TEXT NOT NULL COMMENT '标准答案',
                                    `keywords` VARCHAR(500) DEFAULT NULL COMMENT '检索关键词',
                                    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态',
                                    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    KEY `idx_category` (`category`),
                                    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ知识库表';
