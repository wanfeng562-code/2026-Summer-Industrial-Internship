-- =============================================
-- 工单管理系统数据库脚本
-- 数据库：ticket_system
-- =============================================

CREATE DATABASE IF NOT EXISTS `ticket_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ticket_system`;

-- -------------------------------------------
-- 用户表
-- -------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                            `password` VARCHAR(200) NOT NULL COMMENT '密码',
                            `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                            `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                            `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                            `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
                            `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/AGENT/ADMIN',
                            `reputation_score` INT NOT NULL DEFAULT 100 COMMENT '信誉分(0-100)',
                            `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- -------------------------------------------
-- 订单表
-- -------------------------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                          `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
                          `user_id` BIGINT NOT NULL COMMENT '用户ID',
                          `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
                          `quantity` INT NOT NULL DEFAULT 1 COMMENT '商品数量',
                          `unit_price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
                          `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
                          `order_status` VARCHAR(30) NOT NULL COMMENT '订单状态: PENDING/PAID/SHIPPED/DELIVERED/COMPLETED/CANCELLED',
                          `payment_status` VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态: UNPAID/PAID/REFUNDED',
                          `logistics_status` VARCHAR(20) DEFAULT NULL COMMENT '物流状态: PENDING/SHIPPED/DELIVERED/RECEIVED',
                          `logistics_no` VARCHAR(50) DEFAULT NULL COMMENT '物流单号',
                          `order_time` DATETIME DEFAULT NULL COMMENT '下单时间',
                          `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
                          `deliver_time` DATETIME DEFAULT NULL COMMENT '发货时间',
                          `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
                          `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_order_no` (`order_no`),
                          KEY `idx_user_id` (`user_id`),
                          KEY `idx_order_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- -------------------------------------------
-- 工单表
-- -------------------------------------------
DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工单ID',
                          `ticket_no` VARCHAR(50) NOT NULL COMMENT '工单编号',
                          `user_id` BIGINT NOT NULL COMMENT '创建用户ID',
                          `agent_id` BIGINT DEFAULT NULL COMMENT '处理客服ID',
                          `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
                          `title` VARCHAR(200) NOT NULL COMMENT '工单标题',
                          `description` TEXT NOT NULL COMMENT '工单描述',
                          `category` VARCHAR(30) NOT NULL COMMENT '分类: REFUND/LOGISTICS/DAMAGE/INVOICE/OTHER',
                          `status` VARCHAR(30) NOT NULL DEFAULT 'AI_PROCESSING' COMMENT '状态: AI_PROCESSING/MANUAL_REVIEW/RESOLVED/CLOSED',
                          `priority` VARCHAR(10) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级: LOW/MEDIUM/HIGH/URGENT',
                          `sla_warning` TINYINT NOT NULL DEFAULT 0 COMMENT 'SLA预警: 0-正常 1-已预警',
                          `sla_escalated` TINYINT NOT NULL DEFAULT 0 COMMENT 'SLA升级: 0-正常 1-已升级',
                          `sla_deadline` DATETIME DEFAULT NULL COMMENT 'SLA截止时间',
                          `resolve_time` DATETIME DEFAULT NULL COMMENT '解决时间',
                          `close_time` DATETIME DEFAULT NULL COMMENT '关闭时间',
                          `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_ticket_no` (`ticket_no`),
                          KEY `idx_user_id` (`user_id`),
                          KEY `idx_agent_id` (`agent_id`),
                          KEY `idx_status` (`status`),
                          KEY `idx_category` (`category`),
                          KEY `idx_sla_deadline` (`sla_deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单表';

-- -------------------------------------------
-- 工单消息表
-- -------------------------------------------
DROP TABLE IF EXISTS `ticket_message`;
CREATE TABLE `ticket_message` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                  `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
                                  `user_id` BIGINT NOT NULL COMMENT '发送者用户ID',
                                  `sender_type` VARCHAR(20) NOT NULL COMMENT '发送者类型: USER/AGENT/AI/SYSTEM',
                                  `message_type` VARCHAR(30) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT/AI_REPLY/AI_SUGGESTION/SYSTEM',
                                  `content` TEXT NOT NULL COMMENT '消息内容',
                                  `ai_process_result` TEXT DEFAULT NULL COMMENT 'AI处理结果JSON',
                                  `human_feedback` VARCHAR(200) DEFAULT NULL COMMENT '人工反馈',
                                  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_ticket_id` (`ticket_id`),
                                  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单消息表';

-- -------------------------------------------
-- 工单操作日志表
-- -------------------------------------------
DROP TABLE IF EXISTS `ticket_operation_log`;
CREATE TABLE `ticket_operation_log` (
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

-- -------------------------------------------
-- 售后策略表
-- -------------------------------------------
DROP TABLE IF EXISTS `after_sale_policy`;
CREATE TABLE `after_sale_policy` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '策略ID',
                                     `policy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
                                     `category` VARCHAR(30) NOT NULL COMMENT '适用分类: REFUND/LOGISTICS/DAMAGE/INVOICE/OTHER',
                                     `condition_type` VARCHAR(30) NOT NULL COMMENT '条件类型: ALWAYS/AMOUNT_REPUTATION/AMOUNT/REPUTATION',
                                     `min_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '最小金额',
                                     `max_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '最大金额',
                                     `min_reputation` INT DEFAULT NULL COMMENT '最低信誉分',
                                     `action` VARCHAR(30) NOT NULL COMMENT '处理动作: AUTO_APPROVE/AUTO_REPLY/MANUAL',
                                     `reply_template` TEXT DEFAULT NULL COMMENT '回复模板',
                                     `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级(越小越优先)',
                                     `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态: 0-禁用 1-启用',
                                     `sla_hours` INT DEFAULT NULL COMMENT '命中策略后的SLA时长（小时）',
                                     `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_category` (`category`),
                                     KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='售后策略表';

-- -------------------------------------------
-- FAQ知识库表
-- -------------------------------------------
DROP TABLE IF EXISTS `faq`;
CREATE TABLE `faq` (
                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
                       `category` VARCHAR(30) NOT NULL COMMENT '分类: REFUND/LOGISTICS/DAMAGE/INVOICE/OTHER',
                       `question` VARCHAR(500) NOT NULL COMMENT '常见问题',
                       `answer` TEXT NOT NULL COMMENT '标准答案',
                       `keywords` VARCHAR(500) DEFAULT NULL COMMENT '检索关键词，逗号分隔',
                       `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态: 0-禁用 1-启用',
                       `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                       `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                       PRIMARY KEY (`id`),
                       KEY `idx_category` (`category`),
                       KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ知识库表';

-- -------------------------------------------
-- AI处理日志表
-- -------------------------------------------
DROP TABLE IF EXISTS `ai_process_log`;
CREATE TABLE `ai_process_log` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                      `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
                                      `message_id` BIGINT DEFAULT NULL COMMENT '消息ID',
                                      `intent_result` VARCHAR(30) DEFAULT NULL COMMENT '意图识别结果',
                                      `policy_matched` VARCHAR(100) DEFAULT NULL COMMENT '匹配到的策略',
                                      `ai_action` VARCHAR(30) DEFAULT NULL COMMENT 'AI执行动作',
                                      `ai_reply` TEXT DEFAULT NULL COMMENT 'AI生成回复',
                                      `process_detail` TEXT DEFAULT NULL COMMENT '处理详情',
                                       `execution_time` INT DEFAULT NULL COMMENT '执行耗时(ms)',
                                       `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_ticket_id` (`ticket_id`),
                                       KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI处理日志表';
